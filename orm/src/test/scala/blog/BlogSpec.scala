package blog

import scalikejdbc._, SQLInterpolation._
import scalikejdbc.scalatest.AutoRollback

import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers

class BlogSpec extends fixture.FunSpec with ShouldMatchers
    with Connection
    with CreateTables
    with AutoRollback {

  override def db(): DB = NamedDB('blog).toDB()

  override def fixture(implicit session: DBSession) {
    val postId = Post.createWithAttributes('title -> "Hello World!", 'body -> "This is the first entry...")
    val scalaTagId = Tag.createWithAttributes('name -> "Scala")
    val rubyTagId = Tag.createWithAttributes('name -> "Ruby")
    val pt = PostTag.column
    insert.into(PostTag).namedValues(pt.postId -> postId, pt.tagId -> scalaTagId).toSQL.update.apply()
    insert.into(PostTag).namedValues(pt.postId -> postId, pt.tagId -> rubyTagId).toSQL.update.apply()
  }

  describe("hasManyThrough without byDefault") {
    it("should work as expected") { implicit session =>
      val id = Post.limit(1).apply().head.id
      val post = Post.joins(Post.tagsRef).findById(id)
      post.get.tags.size should equal(2)
    }

    it("should work when joining twice") { implicit session =>
      val id = Post.limit(1).apply().head.id
      val post = Post.joins(Post.tagsRef, Post.tagsRef).findById(id)
      post.get.tags.size should equal(2)
    }

    it("should work with BigDecimal") { implicit session =>
      val post = Post.limit(1).apply().head
      Post.updateById(post.id).withAttributes('viewCount -> 123)
      Post.findById(post.id).get.viewCount should equal(123)
    }
  }
}
