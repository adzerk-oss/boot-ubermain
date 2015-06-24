package adzerk;

import java.net.URL;
import java.net.URLClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;


import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

class MainSploder {

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
    FileInputStream fis = new FileInputStream(jar);
    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
    ZipEntry e;
    int size;
    byte[] buffer = new byte[2048];
    while((e = zis.getNextEntry()) != null) {
      if(e.getName().endsWith(".jar")) {
        FileOutputStream os = new FileOutputStream(tmp.getAbsolutePath() + File.separator + e.getName());
        BufferedOutputStream bos = new BufferedOutputStream(os, buffer.length);
        while((size = zis.read(buffer, 0, buffer.length)) != -1) {
          os.write(buffer, 0, size);
        }
        os.flush();
        os.close();
      }
    }
    zis.close();
    return tmp;
  }

  public static URL[] getJarURLs(File jar) throws Exception {
    File[] jars = copyJarsFrom(jar).listFiles();
    URL[] urls = new URL[jars.length];
    for(int i = 0; i < jars.length; i++) {
      String urlPath = "jar:file://" + jars[i].getAbsolutePath() + "!/";
      urls[i] = new URL(urlPath);
    }
    return urls;
  }

  @SuppressWarnings("unchecked")
  public static void main(String [] args) throws Exception {
    File thisJar      = getThisJar();
    URL[] jarUrls     = getJarURLs(thisJar);
    ClassLoader cl    = new URLClassLoader(jarUrls, Thread.currentThread().getContextClassLoader());

    Class rtClass     = cl.loadClass("org.projectodd.shimdandy.ClojureRuntimeShim");

    Method newRuntime = rtClass.getMethod("newRuntime", ClassLoader.class, String.class);
    Object runtime    = newRuntime.invoke(null, cl, "$namespace$/$name$");

    Method require    = runtime.getClass().getMethod("require", new Class[]{String[].class});

    Method invoke     = runtime.getClass().getMethod("invoke", String.class, Object.class);

    Object applyVar   = invoke.invoke(runtime, "clojure.core/resolve",
                                    invoke.invoke(runtime, "clojure.core/symbol", "clojure.core/apply"));

    require.invoke(runtime, new Object[]{new String[]{"$namespace$"}});

    Object mainVar     = invoke.invoke(runtime, "clojure.core/resolve",
                                   invoke.invoke(runtime, "clojure.core/symbol", "$namespace$/$name$"));

    Method applyInvoke = applyVar.getClass().getMethod("invoke", Object.class, Object.class);

    applyInvoke.invoke(applyVar, mainVar, args);
  }
}
