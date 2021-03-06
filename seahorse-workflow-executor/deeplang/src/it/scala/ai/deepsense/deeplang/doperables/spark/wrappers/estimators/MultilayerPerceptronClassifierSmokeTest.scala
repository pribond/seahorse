/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators

import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class MultilayerPerceptronClassifierSmokeTest extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "MultilayerPerceptronClassifier"

  override val estimator = new MultilayerPerceptronClassifier()

  import estimator._

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    labelColumn -> NameSingleColumnSelection("myRating"),
    layersParam -> Array(3.0, 2.0, 1.0),
    maxIterations -> 120.0,
    predictionColumn -> "prediction",
    seed -> 100.0,
    tolerance -> 2E-5
  )
}
