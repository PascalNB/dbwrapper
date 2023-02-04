package com.pascalnb.dbwrapper.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ParseField(val value: String = "")