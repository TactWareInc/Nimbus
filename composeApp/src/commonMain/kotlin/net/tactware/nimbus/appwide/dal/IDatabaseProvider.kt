package net.tactware.nimbus.appwide.dal

interface IDatabaseProvider<T> {

    val database : T
}