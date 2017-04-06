package io.github.retronym.classpathshrinker

import java.io.ByteArrayOutputStream
import java.nio.file.{Files, Paths}

private[classpathshrinker] object BazelIntegration {

  case class BazelTarget(label: String, target: String)

  def generateJdeps(usedDeps: List[String], outputPath: String): Unit = {
    val dependeciesBuilder = Deps.Dependencies.newBuilder()

    usedDeps.foreach(dep => {
      val dependecyBuilder = Deps.Dependency.newBuilder()
      dependeciesBuilder.addDependency(dependecyBuilder.setPath(dep).setKind(Deps.Dependency.Kind.EXPLICIT).build())
    })

    extractBazelTarget(System.getProperty("sun.java.command")).foreach {
      case BazelTarget(label, target) =>
        val proto = dependeciesBuilder.setRuleLabel(label).build()

        val baos = new ByteArrayOutputStream()
        proto.writeTo(baos)
        Files.write(Paths.get(BazelIntegration.extractJdepsPath(outputPath, target)), baos.toByteArray)
    }
  }

  def extractBazelTarget(javaCommand: String): Option[BazelTarget] = {
    require(javaCommand != null)
    // io.bazel.rulesscala.scalac.ScalaCInvoker @bazel-out/local-fastbuild/bin/some_path/target_worker_input
    val needle = "@bazel-out/local-fastbuild/bin/"
    val index = javaCommand.lastIndexOf(needle)
    if (index == -1) {
      None
    } else {
      // expected "some_path/target"
      val rulePath = javaCommand.substring(index + needle.length).stripSuffix("_worker_input")
      val separatorIndex = rulePath.lastIndexOf('/')
      val path = if (separatorIndex == -1) "" else rulePath.substring(0, separatorIndex)
      val target = rulePath.substring(separatorIndex + 1)

      Some(BazelTarget(s"//$path:$target", target))
    }
  }

  def extractJdepsPath(tmpPath: String, target: String): String = {
    // /private/var/tmp/_bazel_shvar/881b67be136316e5c1a605b940f0d7da/execroot/bazel-one/bazel-out/local-fastbuild/bin/some_path/tmp6632138641537149462
    val needle = "/tmp"
    val index = tmpPath.lastIndexOf(needle)
    assert(index != -1, "coudn't extract tmpdir name from path")
    assert(tmpPath.lastIndexOf('/') == index, "unexpected subdirectory in tmpdir")
    //  /private/var/tmp/_bazel_shvar/881b67be136316e5c1a605b940f0d7da/execroot/bazel-one/bazel-out/local-fastbuild/bin/some_path
    val prefixPath = tmpPath.substring(0, index)
    s"$prefixPath/$target.jdeps"
  }
}
