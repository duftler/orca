/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.igor.tasks

import com.netflix.spinnaker.kork.artifacts.model.Artifact
import com.netflix.spinnaker.orca.TaskResult
import com.netflix.spinnaker.orca.igor.BuildService
import com.netflix.spinnaker.orca.pipeline.model.Execution
import com.netflix.spinnaker.orca.pipeline.model.Stage
import spock.lang.Specification
import spock.lang.Subject

class GetBuildArtifactsTaskSpec extends Specification {
  def buildService = Mock(BuildService)
  def testArtifact = Artifact.builder().name("my-artifact").build()

  def BUILD_NUMBER = 4
  def MASTER = "builds"
  def JOB = "orca"
  def PROPERTY_FILE = "my-file"

  @Subject
  GetBuildArtifactsTask task = new GetBuildArtifactsTask(buildService)

  def "retreives artifacts and adds them to the stage outputs"() {
    given:
    def stage = createStage(PROPERTY_FILE)

    when:
    TaskResult result = task.execute(stage)
    def artifacts = result.getOutputs().get("artifacts") as List<Artifact>

    then:
    1 * buildService.getArtifacts(BUILD_NUMBER, PROPERTY_FILE, MASTER, JOB) >> [testArtifact]
    artifacts.size() == 1
    artifacts.get(0).getName() == "my-artifact"
  }

  def "handles an empty artifact list"() {
    given:
    def stage = createStage(PROPERTY_FILE)

    when:
    TaskResult result = task.execute(stage)
    def artifacts = result.getOutputs().get("artifacts") as List<Artifact>

    then:
    1 * buildService.getArtifacts(BUILD_NUMBER, PROPERTY_FILE, MASTER, JOB) >> []
    artifacts.size() == 0
  }

  def "does not fetch artifacts if the property file is empty"() {
    given:
    def stage = createStage("")

    when:
    TaskResult result = task.execute(stage)

    then:
    0 * buildService.getArtifacts(*_)
    result.outputs.size() == 0
  }

  def "does not fetch artifacts if the property file is null"() {
    given:
    def stage = createStage(null)

    when:
    TaskResult result = task.execute(stage)

    then:
    0 * buildService.getArtifacts(*_)
    result.outputs.size() == 0
  }

  def createStage(String propertyFile) {
    return new Stage(Stub(Execution), "jenkins", [
      master: MASTER,
      job: JOB,
      buildNumber: BUILD_NUMBER,
      propertyFile: propertyFile
    ])
  }
}
