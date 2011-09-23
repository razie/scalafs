package com.razie.pubstage.life

object WorkerBaseNone extends WorkerBase {
  override val delegate = null
  override def dying(): Boolean = {false}
  override def candienow(): Unit = {}
  override def updateProgress(newProgress: Int, newProgressCode: String): Unit = {}
}
