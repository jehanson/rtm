import play.api.mvc.WithFilters
import play.extras.iteratees.GzipFilter

object Global extends WithFilters(new GzipFilter()) {
}
