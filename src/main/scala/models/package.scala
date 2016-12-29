import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive

package object models {
  type IdType = Long Refined Positive
}
