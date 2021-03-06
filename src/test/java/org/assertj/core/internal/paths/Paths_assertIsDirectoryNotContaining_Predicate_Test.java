/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2020 the original author or authors.
 */
package org.assertj.core.internal.paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.error.ShouldBeDirectory.shouldBeDirectory;
import static org.assertj.core.error.ShouldExist.shouldExist;
import static org.assertj.core.error.ShouldNotContain.directoryShouldNotContain;
import static org.assertj.core.internal.Paths.toPathNames;
import static org.assertj.core.util.AssertionsUtil.expectAssertionError;
import static org.assertj.core.util.FailureMessages.actualIsNull;
import static org.assertj.core.util.Lists.emptyList;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.assertj.core.api.AssertionInfo;
import org.assertj.core.internal.Paths;
import org.junit.jupiter.api.Test;

/**
 * Tests for <code>{@link Paths#assertIsDirectoryNotContaining(AssertionInfo, Path, Predicate)}</code>
 *
 * @author Valeriy Vyrva
 */
class Paths_assertIsDirectoryNotContaining_Predicate_Test extends MockPathsBaseTest {

  /**
   * We will check count call to {@link Path#getFileName()}
   */
  private static final Predicate<Path> JAVA_SOURCE = path -> Optional.ofNullable(path.getFileName())
                                                                     .map(Path::toString)
                                                                     .filter(fileName -> fileName.endsWith(".java"))
                                                                     .isPresent();

  @Test
  void should_pass_if_actual_does_not_contain_files_matching_the_given_filter() {
    // GIVEN
    Path file = mockEmptyRegularFile("root", "Test.class");
    List<Path> items = list(file);
    Path actual = mockDirectory("root", items);
    // THEN
    paths.assertIsDirectoryNotContaining(INFO, actual, JAVA_SOURCE);
  }

  @Test
  void should_pass_if_actual_is_empty() {
    // GIVEN
    List<Path> items = emptyList();
    Path actual = mockDirectory("root", items);
    // THEN
    paths.assertIsDirectoryNotContaining(INFO, actual, JAVA_SOURCE);
  }

  @Test
  void should_throw_error_if_filter_is_null() {
    // GIVEN
    Predicate<Path> filter = null;
    // THEN
    assertThatNullPointerException().isThrownBy(() -> paths.assertIsDirectoryNotContaining(INFO, null, filter))
                                    .withMessage("The paths filter should not be null");
  }

  @Test
  void should_fail_if_actual_is_null() {
    // GIVEN
    Path actual = null;
    // WHEN
    AssertionError error = expectAssertionError(() -> paths.assertIsDirectoryNotContaining(INFO, actual, JAVA_SOURCE));
    // THEN
    assertThat(error).hasMessage(actualIsNull());
  }

  @Test
  void should_fail_if_actual_does_not_exist() {
    // GIVEN
    given(nioFilesWrapper.exists(actual)).willReturn(false);
    // WHEN
    expectAssertionError(() -> paths.assertIsDirectoryNotContaining(INFO, actual, JAVA_SOURCE));
    // THEN
    verify(failures).failure(INFO, shouldExist(actual));
  }

  @Test
  void should_fail_if_actual_exists_but_is_not_directory() {
    // GIVEN
    given(nioFilesWrapper.exists(actual)).willReturn(true);
    given(nioFilesWrapper.isDirectory(actual)).willReturn(false);
    // WHEN
    expectAssertionError(() -> paths.assertIsDirectoryNotContaining(INFO, actual, JAVA_SOURCE));
    // THEN
    verify(failures).failure(INFO, shouldBeDirectory(actual));
  }

  @Test
  void should_throw_runtime_error_wrapping_caught_IOException() throws IOException {
    // GIVEN
    IOException cause = new IOException();
    given(nioFilesWrapper.exists(actual)).willReturn(true);
    given(nioFilesWrapper.isDirectory(actual)).willReturn(true);
    given(nioFilesWrapper.newDirectoryStream(eq(actual), any())).willThrow(cause);
    // WHEN
    Throwable error = catchThrowable(() -> paths.assertIsDirectoryNotContaining(INFO, actual, JAVA_SOURCE));
    // THEN
    assertThat(error).isInstanceOf(UncheckedIOException.class)
                     .hasCause(cause);
  }

  @Test
  void should_fail_if_one_actual_file_matches_the_filter() {
    // GIVEN
    Path file = mockEmptyRegularFile("Test.java");
    List<Path> items = list(file);
    Path actual = mockDirectory("root", items);
    // WHEN
    expectAssertionError(() -> paths.assertIsDirectoryNotContaining(INFO, actual, JAVA_SOURCE));
    // THEN
    verify(failures).failure(INFO, directoryShouldNotContain(actual, toPathNames(items), "the given filter"));
  }

  @Test
  void should_fail_if_all_actual_files_match_the_filter() {
    // GIVEN
    Path file1 = mockEmptyRegularFile("Test.java");
    Path file2 = mockEmptyRegularFile("Utils.java");
    List<Path> items = list(file1, file2);
    Path actual = mockDirectory("root", items);
    // WHEN
    expectAssertionError(() -> paths.assertIsDirectoryNotContaining(INFO, actual, JAVA_SOURCE));
    // THEN
    verify(failures).failure(INFO, directoryShouldNotContain(actual, toPathNames(items), "the given filter"));
  }

  @Test
  void should_fail_if_some_actual_files_match_the_filter() {
    // GIVEN
    Path file1 = mockEmptyRegularFile("Test.class");
    Path file2 = mockEmptyRegularFile("Test.java");
    Path file3 = mockEmptyRegularFile("Utils.class");
    Path file4 = mockEmptyRegularFile("Utils.java");
    Path file5 = mockEmptyRegularFile("application.yml");
    List<Path> items = list(file1, file2, file3, file4, file5);
    Path actual = mockDirectory("root", items);
    // WHEN
    expectAssertionError(() -> paths.assertIsDirectoryNotContaining(INFO, actual, JAVA_SOURCE));
    // THEN
    verify(failures).failure(INFO, directoryShouldNotContain(actual, toPathNames(list(file2, file4)), "the given filter"));
  }

}
