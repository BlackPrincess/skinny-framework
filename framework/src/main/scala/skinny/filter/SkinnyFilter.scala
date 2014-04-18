package skinny.filter

import skinny.controller.{ SkinnyControllerBase, SkinnyController }

/**
 * Skinny Filter.
 *
 * For example:
 *
 * {{{
 *   class BooksController extends SkinnyController
 *     with TxPerRequestFiler
 *     with SkinnyFilterActivation {
 *
 *     // within a transaction
 *     def changeTitle = {
 *       if (...) {
 *         throw new UnexpectedErrorException
 *         // rollback
 *       } else {
 *         redirect(s"/books/\${id}")
 *         // commit
 *       }
 *     }
 *   }
 * }}}
 *
 * If you use Scatatra's filter - before/after, be careful. It's pretty tricky.
 * Because Scalatra's filters would be applied for all the controllers difined below in ScalatraBootstrap.
 * Just using beforeAction/afterAction is highly recommended.
 */
trait SkinnyFilter extends SkinnyControllerBase with SkinnyFilterActivation

