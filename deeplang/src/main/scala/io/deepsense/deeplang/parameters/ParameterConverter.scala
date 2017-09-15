/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.parameters.exceptions.TypeConversionException

/** Parameters converter used for implicit conversions */
abstract class ParameterConverter[T <: Parameter : ru.TypeTag] {
  /** Returns a function converting a parameter into `T`. */
  def convertPF: PartialFunction[Any, T]

  /** Converts and object or throws TypeConversionException if the object can't be converted. */
  def convert(p: Any): T = {
    convertPF.applyOrElse(p, (_: Any) =>
      throw TypeConversionException(p, ru.typeOf[T].typeSymbol.fullName)
    )
  }
}

/**
 * Object containing implicit conversions of Parameters to target types.
 * These conversions are used to get some specific parameter.
 */
object ParameterConversions {
  implicit object ToStringParameter extends ParameterConverter[StringParameter] {
    def convertPF: PartialFunction[Any, StringParameter] = {
      case p: StringParameter => p
    }
  }

  implicit object ToNumericParameter extends ParameterConverter[NumericParameter] {
    def convertPF: PartialFunction[Any, NumericParameter] = {
      case p: NumericParameter => p
    }
  }

  implicit object ToBooleanParameter extends ParameterConverter[BooleanParameter] {
    def convertPF: PartialFunction[Any, BooleanParameter] = {
      case p: BooleanParameter => p
    }
  }

  implicit object ToChoiceParameter extends ParameterConverter[ChoiceParameter] {
    def convertPF: PartialFunction[Any, ChoiceParameter] = {
      case p: ChoiceParameter => p
    }
  }

  implicit object ToMultipleChoiceParameter
    extends ParameterConverter[MultipleChoiceParameter] {

    def convertPF: PartialFunction[Any, MultipleChoiceParameter] = {
      case p: MultipleChoiceParameter => p
    }
  }

  implicit object ToMultiplicatedParameter
    extends ParameterConverter[ParametersSequence] {

    def convertPF: PartialFunction[Any, ParametersSequence] = {
      case p: ParametersSequence => p
    }
  }

  implicit object ToSingleColumnSelectorParameter
    extends ParameterConverter[SingleColumnSelectorParameter] {

    def convertPF: PartialFunction[Any, SingleColumnSelectorParameter] = {
      case p: SingleColumnSelectorParameter => p
    }
  }

  implicit object ToColumnSelectionParameter
    extends ParameterConverter[ColumnSelectorParameter] {

    def convertPF: PartialFunction[Any, ColumnSelectorParameter] = {
      case p: ColumnSelectorParameter => p
    }
  }
}
