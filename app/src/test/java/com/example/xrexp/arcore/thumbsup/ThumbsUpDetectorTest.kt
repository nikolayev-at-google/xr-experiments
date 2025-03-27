package com.example.xrexp.arcore.thumbsup

import androidx.xr.arcore.HandJointType
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import androidx.xr.arcore.Hand
import androidx.xr.runtime.math.Quaternion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner



@RunWith(MockitoJUnitRunner::class)
class ThumbsUpDetectorTest {

    @Mock
    lateinit var mockHandState: Hand.State

    @Test
    fun `detectThumbsUp returns false when hand is not active`() {
        // Given
        `when`(mockHandState.isActive).thenReturn(false)

        // When
        val result = ThumbsUpDetector.detectThumbsUp(mockHandState)

       // Then
       assertFalse(result.isThumbsUp)
   }

    @Test
    fun `detectThumbsUp returns false when not all joints are available`() {
        // Given
        `when`(mockHandState.isActive).thenReturn(true)
        `when`(mockHandState.handJoints).thenReturn(emptyMap()) // No joints available

        // When
        val result = ThumbsUpDetector.detectThumbsUp(mockHandState)

        // Then
        assertFalse(result.isThumbsUp)
    }

    @Test
    fun `detectThumbsUp returns false when thumb is not pointing up`() {
        // Given
        `when`(mockHandState.isActive).thenReturn(true)
        val joints = createMockHandJoints(
            thumbMetacarpalPose = Pose(Vector3(0f, 0f, 0f), Quaternion()),
            thumbTipPose = Pose(Vector3(0f, 0f, -1f), Quaternion()) //thumb pointing forward
        )

        `when`(mockHandState.handJoints).thenReturn(joints)

        // When
        val result = ThumbsUpDetector.detectThumbsUp(mockHandState)

        // Then
        assertFalse(result.isThumbsUp)
    }

    @Test
    fun `detectThumbsUp returns false when thumb is not extended`() {
        // Given
        `when`(mockHandState.isActive).thenReturn(true)
        val joints = createMockHandJoints(
            thumbMetacarpalPose = Pose(Vector3(0f, 0f, 0f), Quaternion()),
            thumbTipPose = Pose(Vector3(0f, 0.1f, 0.1f), Quaternion()),
            wristPose = Pose(Vector3(0f, -0.5f, 0f), Quaternion())
        )
        `when`(mockHandState.handJoints).thenReturn(joints)

        // When
        val result = ThumbsUpDetector.detectThumbsUp(mockHandState)

        // Then
        assertFalse(result.isThumbsUp)
    }

    @Test
    fun `detectThumbsUp returns false when index is not curled`() {
        // Given
        `when`(mockHandState.isActive).thenReturn(true)

        val joints = createMockHandJoints(
            thumbMetacarpalPose = Pose(Vector3(0f, 0f, 0f), Quaternion()),
            thumbTipPose = Pose(Vector3(0f, 1f, 0f), Quaternion()),
            wristPose = Pose(Vector3(0f, -0.5f, 0f), Quaternion()),
            indexProximalPose = Pose(Vector3(1f, 0.5f, 0f), Quaternion()),
            indexIntermediatePose = Pose(Vector3(1f, 1f, 0f), Quaternion()),
            indexTipPose = Pose(Vector3(1f, 2f, 0f), Quaternion())
        )
        `when`(mockHandState.handJoints).thenReturn(joints)

        // When
        val result = ThumbsUpDetector.detectThumbsUp(mockHandState)

        // Then
        assertFalse(result.isThumbsUp)
    }

    @Test
    fun `detectThumbsUp returns false when middle finger is not curled`() {
        // Given
        `when`(mockHandState.isActive).thenReturn(true)

        val joints = createMockHandJoints(
            thumbMetacarpalPose = Pose(Vector3(0f, 0f, 0f), Quaternion()),
            thumbTipPose = Pose(Vector3(0f, 1f, 0f), Quaternion()),
            wristPose = Pose(Vector3(0f, -0.5f, 0f), Quaternion()),
            indexProximalPose = Pose(Vector3(1f, 0.5f, 0f), Quaternion()),
            indexIntermediatePose = Pose(Vector3(1f, 0.6f, 0f), Quaternion()),
            indexTipPose = Pose(Vector3(1f, 0.7f, 0f), Quaternion()),
            middleProximalPose = Pose(Vector3(2f, 0.5f, 0f), Quaternion()),
            middleIntermediatePose = Pose(Vector3(2f, 1f, 0f), Quaternion()),
            middleTipPose = Pose(Vector3(2f, 2f, 0f), Quaternion())
        )
        `when`(mockHandState.handJoints).thenReturn(joints)

        // When
        val result = ThumbsUpDetector.detectThumbsUp(mockHandState)

        // Then
        assertFalse(result.isThumbsUp)
    }

    @Test
    fun `detectThumbsUp returns true when all conditions are met`() {
        // Given
        `when`(mockHandState.isActive).thenReturn(true)

        val joints = createMockHandJoints(
            thumbMetacarpalPose = Pose(Vector3(0f, 0f, 0f), Quaternion()),
            thumbTipPose = Pose(Vector3(0f, 1f, 0f), Quaternion()),
            wristPose = Pose(Vector3(0f, -0.5f, 0f), Quaternion()),

            // Curled index finger
            indexProximalPose = Pose(Vector3(1f, 0.5f, 0f), Quaternion()),
            indexIntermediatePose = Pose(Vector3(1f, 0.6f, 0f), Quaternion()),
            indexTipPose = Pose(Vector3(1f, 0.7f, 0f), Quaternion()),

            // Curled middle finger
            middleProximalPose = Pose(Vector3(2f, 0.5f, 0f), Quaternion()),
            middleIntermediatePose = Pose(Vector3(2f, 0.6f, 0f), Quaternion()),
            middleTipPose = Pose(Vector3(2f, 0.7f, 0f), Quaternion()),

            // Curled ring finger
            ringProximalPose = Pose(Vector3(3f, 0.5f, 0f), Quaternion()),
            ringIntermediatePose = Pose(Vector3(3f, 0.6f, 0f), Quaternion()),
            ringTipPose = Pose(Vector3(3f, 0.7f, 0f), Quaternion()),

            // Curled little finger
            littleProximalPose = Pose(Vector3(4f, 0.5f, 0f), Quaternion()),
            littleIntermediatePose = Pose(Vector3(4f, 0.6f, 0f), Quaternion()),
            littleTipPose = Pose(Vector3(4f, 0.7f, 0f), Quaternion())
        )
        `when`(mockHandState.handJoints).thenReturn(joints)

        // When
        val result = ThumbsUpDetector.detectThumbsUp(mockHandState)

        // Then
        assertTrue(result.isThumbsUp)
    }

    private fun createMockHandJoints(
            wristPose: Pose = Pose(),
            palmPose: Pose = Pose(),
            thumbMetacarpalPose: Pose = Pose(),
            thumbTipPose: Pose = Pose(),
            indexTipPose: Pose = Pose(),
            middleTipPose: Pose = Pose(),
            ringTipPose: Pose = Pose(),
            littleTipPose: Pose = Pose(),
            indexIntermediatePose: Pose = Pose(),
            middleIntermediatePose: Pose = Pose(),
            ringIntermediatePose: Pose = Pose(),
            littleIntermediatePose: Pose = Pose(),
            indexMetacarpalPose: Pose = Pose(),
            indexProximalPose: Pose = Pose(),
            middleProximalPose: Pose = Pose(),
            ringProximalPose: Pose = Pose(),
            littleProximalPose: Pose = Pose()
        ): Map<HandJointType, Pose> {
            val joints = mutableMapOf<HandJointType, Pose>()
            joints[HandJointType.WRIST] = wristPose
            joints[HandJointType.PALM] = palmPose
            joints[HandJointType.THUMB_METACARPAL] = thumbMetacarpalPose
            joints[HandJointType.THUMB_TIP] = thumbTipPose
            joints[HandJointType.INDEX_TIP] = indexTipPose
            joints[HandJointType.MIDDLE_TIP] = middleTipPose
            joints[HandJointType.RING_TIP] = ringTipPose
            joints[HandJointType.LITTLE_TIP] = littleTipPose
            joints[HandJointType.INDEX_INTERMEDIATE] = indexIntermediatePose
            joints[HandJointType.MIDDLE_INTERMEDIATE] = middleIntermediatePose
            joints[HandJointType.RING_INTERMEDIATE] = ringIntermediatePose
            joints[HandJointType.LITTLE_INTERMEDIATE] = littleIntermediatePose
            joints[HandJointType.INDEX_METACARPAL] = indexMetacarpalPose
            joints[HandJointType.INDEX_PROXIMAL] = indexProximalPose
            joints[HandJointType.MIDDLE_PROXIMAL] = middleProximalPose
            joints[HandJointType.RING_PROXIMAL] = ringProximalPose
            joints[HandJointType.LITTLE_PROXIMAL] = littleProximalPose
            return joints
        }
}