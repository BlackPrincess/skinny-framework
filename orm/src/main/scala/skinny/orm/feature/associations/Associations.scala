package skinny.orm.feature.associations

import scala.language.existentials

import skinny.orm.feature._
import scala.collection.mutable

/**
 * Association.
 *
 * @tparam Entity entity
 */
sealed trait Association[Entity] {

  /**
   * ORM mapper instance.
   */
  def mapper: AssociationsFeature[Entity]

  /**
   * Join definitions.
   */
  def joinDefinitions: mutable.LinkedHashSet[JoinDefinition[_]]

  /**
   * Enables extractor by default.
   */
  def setExtractorByDefault(): Unit

  /**
   * Activates this association by default.
   */
  def byDefault: Association[Entity] = {
    joinDefinitions.foreach { joinDef =>
      joinDef.byDefault(joinDef.enabledEvenIfAssociated)
      mapper.defaultJoinDefinitions.add(joinDef)
    }
    setExtractorByDefault()
    this
  }

}

/**
 * BelongsTo relation.
 *
 * @param mapper mapper
 * @param joinDefinitions join definitions
 * @param extractor extractor
 * @tparam Entity entity
 */
case class BelongsToAssociation[Entity](
    mapper: AssociationsFeature[Entity],
    joinDefinitions: mutable.LinkedHashSet[JoinDefinition[_]],
    extractor: BelongsToExtractor[Entity]) extends Association[Entity] {

  override def setExtractorByDefault() = mapper.setAsByDefault(extractor)

  def includes[A](merge: (Seq[Entity], Seq[A]) => Seq[Entity]): BelongsToAssociation[Entity] = {
    this.copy(extractor = extractor.copy(includesMerge = merge.asInstanceOf[(Seq[Entity], Seq[_]) => Seq[Entity]]))
  }

  mapper.associations.add(this)
}

/**
 * HasOne association.
 *
 * @param mapper mapper
 * @param joinDefinitions join definitions
 * @param extractor extractor
 * @tparam Entity entity
 */
case class HasOneAssociation[Entity](
    mapper: AssociationsFeature[Entity],
    joinDefinitions: mutable.LinkedHashSet[JoinDefinition[_]],
    extractor: HasOneExtractor[Entity]) extends Association[Entity] {

  override def setExtractorByDefault() = mapper.setAsByDefault(extractor)

  def includes[A](merge: (Seq[Entity], Seq[A]) => Seq[Entity]): HasOneAssociation[Entity] = {
    this.copy(extractor = extractor.copy(includesMerge = merge.asInstanceOf[(Seq[Entity], Seq[_]) => Seq[Entity]]))
  }

  mapper.associations.add(this)
}

/**
 * HasMany association.
 *
 * @param mapper mapper
 * @param joinDefinitions join definitions
 * @param extractor extractor
 * @tparam Entity entity
 */
case class HasManyAssociation[Entity](
    mapper: AssociationsFeature[Entity],
    joinDefinitions: mutable.LinkedHashSet[JoinDefinition[_]],
    extractor: HasManyExtractor[Entity]) extends Association[Entity] {

  override def setExtractorByDefault() = mapper.setAsByDefault(extractor)

  def includes[A](merge: (Seq[Entity], Seq[A]) => Seq[Entity]): HasManyAssociation[Entity] = {
    this.copy(extractor = extractor.copy(includesMerge = merge.asInstanceOf[(Seq[Entity], Seq[_]) => Seq[Entity]]))
  }

  mapper.associations.add(this)
}
