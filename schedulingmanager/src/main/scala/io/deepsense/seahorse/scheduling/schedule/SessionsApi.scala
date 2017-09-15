/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.schedule

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.LoggerForCallerClass
import io.deepsense.graph.nodestate.name.NodeStatusName
import io.deepsense.models.workflows.Workflow
import io.deepsense.seahorse.scheduling.SchedulingManagerConfig
import io.deepsense.sessionmanager.rest.client.SessionManagerClient
import io.deepsense.sessionmanager.service.Status

private[schedule] object SessionsApi {
  import scala.concurrent.ExecutionContext.Implicits.global

  private[this] val logger = LoggerForCallerClass()

  private[this] val sessionManagerConfig = SchedulingManagerConfig.config.getConfig("session-manager")
  private[this] lazy val sessionManagerClient = {
    val url: URL = new URL(sessionManagerConfig.getString("url") + "/v1/sessions/")
    logger.info(s"Creating sessions client with url $url.")
    val timeout = Timeout(FiniteDuration(sessionManagerConfig.getDuration("timeout").toMillis, TimeUnit.MILLISECONDS))
    new SessionManagerClient(
      mandatoryUserId = RunWorkflowJobContext.userId,
      mandatoryUserName = RunWorkflowJobContext.userName,
      apiUrl = url,
      credentials = None)(
      RunWorkflowJobContext.actorSystem, timeout)
  }

  def startSession(id: Workflow.Id, clusterDetails: ClusterDetails): Future[Unit] = {
    logger.info(s"Creating session for $id with cluster: $clusterDetails.")
    def sessionStatus(): Future[Status.Value] = sessionManagerClient.fetchSession(id).map(_.status)
    def retryUntilFinished(): Future[Unit] = sessionStatus().flatMap { status =>
      if (status == Status.Running) {
        logger.info(s"Session for $id created successfully.")
        Future.successful(())
      } else if (status == Status.Error) {
        logger.error(s"There was an error while creating a session for $id.")
        Future.failed(new RuntimeException(s"Could not create a session for $id."))
      } else {
        Thread.sleep(5000)
        logger.info(s"Session for workflow $id isn't running yet; asking for status again.")
        retryUntilFinished()
      }
    }
    sessionManagerClient.createSession(id, clusterDetails).flatMap(_ => retryUntilFinished())
  }

  def deleteSession(id: Workflow.Id): Future[Unit] = {
    logger.info(s"Deleting session for $id.")
    sessionManagerClient.deleteSession(id).map(_ => ())
  }

  def runWorkflow(id: Workflow.Id): Future[Unit] = {
    val notFinishedStatuses = Set[NodeStatusName](NodeStatusName.Draft, NodeStatusName.Running, NodeStatusName.Queued)
    def isFinished(): Future[Boolean] = sessionManagerClient.queryNodeStatuses(id).map { response =>
      logger.info(s"Running status of workflow $id: $response")
      response.nodeStatuses.exists { statusesMap =>
        statusesMap
          .filterKeys(notFinishedStatuses.contains)
          .values
          .sum == 0
      }
    }

    def retryUntilFinished(): Future[Unit] = isFinished().flatMap { finished =>
      if (finished) {
        logger.info(s"Workflow $id finished running.")
        Future.successful(())
      } else {
        Thread.sleep(5000)
        logger.info(s"Workflow $id didn't finish running yet; asking for status again.")
        retryUntilFinished()
      }
    }

    logger.info(s"Running workflow $id.")
    sessionManagerClient.launchSession(id).flatMap(_ => retryUntilFinished())
  }
}