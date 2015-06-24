package adzerk;

import java.net.URL;
import java.net.URLClassLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import java.lang.reflect.Method;

class MainSploder extends URLClassLoader {

  public MainSploder (URL[] urls) {
    super(urls);
  }

  public static File getThisJar() throws Exception {
    return new File(MainSploder.class.getProtectionDomain().getCodeSource().getLocation().toURI());
  }

  public static File makeTmpDir() throws Exception {
    File tmpDir = File.createTempFile("tempdir", Long.toString(System.nanoTime()));
    tmpDir.delete();
    tmpDir.mkdir();
    return tmpDir;
  }

  public static File copyJarsFrom(File jar) throws Exception {
    File tmp = makeTmpDir();
    ZipInputStream zis = new ZipInputStream(new FileInputStream(getThisJar()));
    ZipEntry e;
    while((e = zis.getNextEntry()) != null) {
      if(e.getName().endsWith(".jar")) {
        FileOutputStream os = new FileOutputStream(tmp.getAbsolutePath() + File.separator + e.getName());
        int data = 0;
        while((data = zis.read()) != -1) { os.write(data); }
        os.close();
      }
    }
    zis.close();
    return tmp;
  }

  public static URL[] getJarURLs() throws Exception {
    File[] jars = copyJarsFrom(getThisJar()).listFiles();
    URL[] urls = new URL[jars.length];
    for(int i = 0; i < jars.length; i++) {
      String urlPath = "jar:file://" + jars[i].getAbsolutePath() + "!/";
      urls[i] = new URL(urlPath);
    }
    return urls;
  }

  @SuppressWarnings("unchecked")
  public static void main(String [] args) throws Exception {
    URL[] jarUrls = getJarURLs();
    MainSploder loader = new MainSploder(jarUrls);
    Thread.currentThread().setContextClassLoader(loader);

    Method invoke;

    Class rt = loader.loadClass("clojure.lang.RT");
    Class sym = loader.loadClass("clojure.lang.Symbol");

    Method var = rt.getMethod("var", String.class, String.class);
    Object REQUIRE = var.invoke(null, "clojure.core", "require");
    invoke = REQUIRE.getClass().getMethod("invoke", Object.class);
    invoke.invoke(REQUIRE, sym.getMethod("create", String.class).invoke(null, "$namespace$"));

    Object MAIN = var.invoke(null, "$namespace$", "$name$");
    Object APPLY = var.invoke(null, "clojure.core", "apply");
    invoke = APPLY.getClass().getMethod("invoke", Object.class, Object.class);
    invoke.invoke(APPLY, MAIN, args);
  }
}
