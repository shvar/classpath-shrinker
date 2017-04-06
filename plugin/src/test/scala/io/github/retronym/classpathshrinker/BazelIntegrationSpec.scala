package io.github.retronym.classpathshrinker

import io.github.retronym.classpathshrinker.BazelIntegration.BazelTarget
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(classOf[JUnit4])
class BazelIntegrationSpec {

  @Test
  def `Extracting bazel label`(): Unit = {
    val javaCommand = "io.bazel.rulesscala.scalac.ScalaCInvoker @bazel-out/local-fastbuild/bin/some_path/target_worker_input"
    assertEquals(Some(BazelTarget("//some_path:target", "target")), BazelIntegration.extractBazelTarget(javaCommand))
  }

  @Test
  def `Extracting bazel root label`(): Unit = {
    val javaCommand = "io.bazel.rulesscala.scalac.ScalaCInvoker @bazel-out/local-fastbuild/bin/target_worker_input"
    assertEquals(Some(BazelTarget("//:target", "target")), BazelIntegration.extractBazelTarget(javaCommand))
  }

  @Test
  def `Not generate jdeps in case of non-basel`(): Unit = {
    val javaCommand = "scalac"
    assertEquals(None, BazelIntegration.extractBazelTarget(javaCommand))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def `Getting assertion in case of missing property`(): Unit = {
    val javaCommand = null
    BazelIntegration.extractBazelTarget(javaCommand)
  }

  @Test
  def `Get path for jdeps`(): Unit = {
    val tmpPath = "/private/var/tmp/_bazel_shvar/881b67be136316e5c1a605b940f0d7da/execroot/bazel-one/bazel-out/local-fastbuild/bin/some_path/tmp6632138641537149462"
    val jdepsPath = "/private/var/tmp/_bazel_shvar/881b67be136316e5c1a605b940f0d7da/execroot/bazel-one/bazel-out/local-fastbuild/bin/some_path/somelabel.jdeps"
    assertEquals(jdepsPath, BazelIntegration.extractJdepsPath(tmpPath, "somelabel"))
  }

  @Test(expected = classOf[AssertionError])
  def `Get assertion for missing tmpdir`(): Unit = {
    BazelIntegration.extractJdepsPath("/dev/null", "somelabel")
  }

  @Test(expected = classOf[AssertionError])
  def `Get assertion for subdir in tmpdir`(): Unit = {
    BazelIntegration.extractJdepsPath("/tmp/some_subdir", "somelabel")
  }

}
