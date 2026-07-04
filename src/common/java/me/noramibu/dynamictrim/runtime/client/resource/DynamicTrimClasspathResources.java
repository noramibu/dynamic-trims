package me.noramibu.dynamictrim.runtime.client.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import me.noramibu.dynamictrim.DynamicTrim;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public final class DynamicTrimClasspathResources {
    private static final String ASSETS_ROOT = "assets/";
    private static final Set<String> RESOURCE_ROOTS = Set.of(
            "assets/minecraft/atlases",
            "assets/minecraft/shaders/core",
            "assets/minecraft/textures/trims/items"
    );
    private static final Set<String> RESOURCE_PATHS = Collections.unmodifiableSet(scanResourcePaths());

    private DynamicTrimClasspathResources() {
    }

    public static Optional<Resource> getResource(ResourceLocation id) {
        String path = toClasspathPath(id);
        if (!RESOURCE_PATHS.contains(path) && getClassLoader().getResource(path) == null) {
            return Optional.empty();
        }
        return Optional.of(createResource(path));
    }

    public static Map<ResourceLocation, Resource> listResources(
            String path, Predicate<ResourceLocation> filter) {
        Map<ResourceLocation, Resource> resources = new TreeMap<>();
        for (String resourcePath : RESOURCE_PATHS) {
            ResourceLocation id = toResourceLocation(resourcePath);
            if (id != null && id.getPath().startsWith(path) && filter.test(id)) {
                resources.put(id, createResource(resourcePath));
            }
        }
        return resources;
    }

    private static Resource createResource(String path) {
        return new Resource(Minecraft.getInstance().getVanillaPackResources(), () -> openResource(path));
    }

    private static InputStream openResource(String path) throws IOException {
        InputStream stream = getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Missing dynamic trim classpath resource: " + path);
        }
        return stream;
    }

    private static String toClasspathPath(ResourceLocation id) {
        return ASSETS_ROOT + id.getNamespace() + "/" + id.getPath();
    }

    private static ResourceLocation toResourceLocation(String classpathPath) {
        if (!classpathPath.startsWith(ASSETS_ROOT)) {
            return null;
        }
        String namespacedPath = classpathPath.substring(ASSETS_ROOT.length());
        int slash = namespacedPath.indexOf('/');
        if (slash <= 0 || slash == namespacedPath.length() - 1) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(
                namespacedPath.substring(0, slash),
                namespacedPath.substring(slash + 1)
        );
    }

    private static Set<String> scanResourcePaths() {
        Set<String> paths = new HashSet<>();
        for (String root : RESOURCE_ROOTS) {
            try {
                Enumeration<URL> urls = getClassLoader().getResources(root);
                while (urls.hasMoreElements()) {
                    scanResourceUrl(paths, root, urls.nextElement());
                }
            } catch (IOException e) {
                DynamicTrim.LOGGER.warn("Could not scan bundled resources under {}", root, e);
            }
        }
        return paths;
    }

    private static void scanResourceUrl(Set<String> paths, String root, URL url) {
        String protocol = url.getProtocol();
        try {
            if (protocol.equals("file")) {
                scanFileResource(paths, root, url.toURI());
            } else if (protocol.equals("jar")) {
                scanJarResource(paths, root, url);
            }
        } catch (IOException | URISyntaxException e) {
            DynamicTrim.LOGGER.warn("Could not scan bundled resources from {}", url, e);
        }
    }

    private static void scanFileResource(Set<String> paths, String root, URI uri) throws IOException {
        Path rootPath = Path.of(uri);
        if (!Files.isDirectory(rootPath)) {
            return;
        }
        try (Stream<Path> files = Files.walk(rootPath)) {
            files.filter(Files::isRegularFile)
                    .map(rootPath::relativize)
                    .map(Path::toString)
                    .map(path -> path.replace('\\', '/'))
                    .map(path -> root + "/" + path)
                    .forEach(paths::add);
        }
    }

    private static void scanJarResource(Set<String> paths, String root, URL url) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        connection.setUseCaches(false);
        try (JarFile jar = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            String prefix = root + "/";
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && name.startsWith(prefix)) {
                    paths.add(name);
                }
            }
        }
    }

    private static ClassLoader getClassLoader() {
        return DynamicTrimClasspathResources.class.getClassLoader();
    }
}
