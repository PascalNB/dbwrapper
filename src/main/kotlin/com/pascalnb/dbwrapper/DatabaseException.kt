package com.pascalnb.dbwrapper

class DatabaseException : RuntimeException {
    constructor(e: Throwable?) : super(e)
    constructor(message: String?) : super(message)
}