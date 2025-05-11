package com.seankenkeremath.uselessui.convention

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class MavenPublishConfigExtension @Inject constructor(objects: ObjectFactory) {
    abstract val version: Property<String>
    abstract val description: Property<String>
}