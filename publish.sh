#!/bin/sh

sbt ++2.10.0 \
  common/publish-signed \
  assets/publish-signed \
  orm/publish-signed \
  factoryGirl/publish-signed \
  validator/publish-signed \
  framework/publish-signed \
  mailer/publish-signed \
  standalone/publish-signed \
  task/publish-signed \
  test/publish-signed \
  freemarker/publish-signed \
  thymeleaf/publish-signed 

