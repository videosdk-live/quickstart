var myApp = angular.module("myApp", ["config"]);

myApp.config(function () {});

myApp.run(function () {});

myApp.directive("topBar", [
  function () {
    return {
      restrict: "E",
      templateUrl: "views/topBar.html",
      transclude: true,
      controller: "myController",
      replace: true,
    };
  },
]);

myApp.directive("joinScreen", [
  function () {
    return {
      restrict: "E",
      templateUrl: "views/joinScreen.html",
      transclude: true,
      controller: "myController",
      replace: true,
    };
  },
]);

myApp.directive("meetingContainer", [
  function () {
    return {
      restrict: "E",
      templateUrl: "views/meetingContainer.html",
      transclude: true,
      controller: "myController",
      replace: true,
    };
  },
]);

myApp.controller("myController", function ($scope, $http, ENV) {
  // variable initialization
  $scope.name = "Homi J. Bhabha";
  $scope.meetingId = "";
  $scope.isWebcamOn = true;
  $scope.isMicOn = true;
  $scope.showMeetingIdError = false;
  $scope.showMeetingScreen = false;
  $scope.showJoinScreen = true;

  $scope.localParticipant = null;
  $scope.participants = [];
  $scope.meeting = null;
  $scope.participantGridContainer = document.getElementById(
    "participant-grid-container"
  );

  $scope.createVideoElement = function (
    stream,
    participant,
    participantMediaElement
  ) {
    var video = document.createElement("video");
    var mediaStream = new MediaStream();
    mediaStream.addTrack(stream.track);
    video.srcObject = mediaStream;
    video.autoplay = true;
    video.id = `v-${participant.id}`;
    video.style.marginTop = "6px";
    video.style.marginLeft = "4px";
    video.style.marginRight = "4px";
    video.style.width = "320px";
    video.style.height = "300px";
    video.style.objectFit = "cover";
    video.style.transform = "rotate('90')";
    video.style.borderRadius = "10px";
    video.setAttribute("playsinline", true);
    var videoElement = document.createElement("div");
    videoElement.setAttribute("id", `video-container-${participant.id}`);
    participantMediaElement.appendChild(videoElement);
    videoElement.appendChild(video);

    var cornerDisplayName = document.createElement("div");
    cornerDisplayName.setAttribute("id", `name-container-${participant.id}`);
    cornerDisplayName.style.position = "absolute";
    cornerDisplayName.style.bottom = "16px";
    cornerDisplayName.style.left = "16px";
    cornerDisplayName.style.color = "white";
    cornerDisplayName.style.backgroundColor = "rgba(0, 0, 0, 0.5)";
    cornerDisplayName.style.padding = "2px";
    cornerDisplayName.style.borderRadius = "2px";
    cornerDisplayName.style.fontSize = "12px";
    cornerDisplayName.style.fontWeight = "bold";
    cornerDisplayName.style.zIndex = "1";
    cornerDisplayName.style.padding = "4px";
    cornerDisplayName.innerHTML =
      participant.displayName.length > 15
        ? participant.displayName.substring(0, 15) + "..."
        : participant.displayName;
    videoElement.appendChild(cornerDisplayName);
  };

  $scope.createAudioElement = function (
    stream,
    participant,
    participantMediaElement
  ) {
    var audio = document.createElement("audio");
    var mediaStream = new MediaStream();
    mediaStream.addTrack(stream.track);
    audio.srcObject = mediaStream;
    audio.autoplay = true;
    audio.muted;
    audio.id = `audio-${participant.id}`;
    var audioElement = document.createElement("div");
    audioElement.setAttribute("id", `audio-container-${participant.id}`);
    participantMediaElement.appendChild(audioElement);
    audioElement.appendChild(audio);
  };

  $scope.createNameElement = function (participant) {
    var nameElement = document.createElement("div");
    nameElement.setAttribute("id", `name-container-${participant.id}`);
    nameElement.innerHTML = participant.displayName.charAt(0).toUpperCase();
    nameElement.style.backgroundColor = "black";
    nameElement.style.color = "white";
    nameElement.style.textAlign = "center";
    nameElement.style.padding = "32px";
    nameElement.style.borderRadius = "100%";
    nameElement.style.fontSize = "20px";

    return nameElement;
  };

  $scope.handleStreamEnabled = function (
    stream,
    participant,
    isLocal,
    participantMediaElement
  ) {
    if (stream.kind == "video") {
      var nameElement = document.getElementById(
        `name-container-${participant.id}`
      );
      participantMediaElement.removeChild(nameElement);
      $scope.createVideoElement(stream, participant, participantMediaElement);
    }
    if (!isLocal) {
      if (stream.kind == "audio") {
        $scope.createAudioElement(stream, participant, participantMediaElement);
      }
    }
  };

  $scope.handleStreamDisabled = function (
    stream,
    participant,
    isLocal,
    participantMediaElement
  ) {
    if (stream.kind == "video") {
      var videoElement = document.getElementById(
        `video-container-${participant.id}`
      );
      var nameElement = $scope.createNameElement(participant);
      participantMediaElement.removeChild(videoElement);
      participantMediaElement.appendChild(nameElement);
    }
    if (!isLocal) {
      if (stream.kind == "audio") {
        var audioElement = document.getElementById(
          `audio-container-${participant.id}`
        );
        participantMediaElement.removeChild(audioElement);
      }
    }
  };

  $scope.createMeeting = function () {
    const url = "https://api.videosdk.live/v2/rooms";
    const config = {
      headers: {
        Authorization: ENV.token,
        "Content-Type": "application/json",
      },
    };

    $http
      .post(url, { name: $scope.name }, config)
      .then(function (response) {
        const { roomId } = response.data;
        $scope.meetingId = roomId;
        $scope.initMeeting();
      })
      .catch(function (error) {
        alert("error", error);
      });
  };

  $scope.validateMeeting = function () {
    const url = `https://api.videosdk.live/v2/rooms/validate/${$scope.meetingId}`;
    const config = {
      headers: {
        Authorization: ENV.token,
        "Content-Type": "application/json",
      },
    };

    $http
      .get(url, config)
      .then(function (response) {
        if (response.data.roomId === $scope.meetingId) {
          $scope.showMeetingIdError = false;
          $scope.initMeeting();
        }
      })
      .catch(function (error) {
        $scope.showMeetingIdError = true;
        console.log("error", error);
      });
  };

  $scope.initMeeting = function () {
    window.VideoSDK.config(ENV.token); // required;

    var meeting = window.VideoSDK.initMeeting({
      meetingId: $scope.meetingId, // required
      name: $scope.name, // required
      micEnabled: true, // optional, default: true
      webcamEnabled: true, // optional, default: true
      maxResolution: "hd",
    });
    meeting.join();
    $scope.meeting = meeting;

    if ($scope.meeting) {
      $scope.handleMeetingEvents($scope.meeting);
      var showJoinScreenMessage = document.createElement("div");
      var topBar = document.getElementById("top-bar");
      topBar.style.display = "none";

      showJoinScreenMessage.setAttribute("id", "show-join-screen-message");
      showJoinScreenMessage.innerHTML = "Please wait to join meeting...";
      showJoinScreenMessage.style.color = "black";
      showJoinScreenMessage.style.fontSize = "20px";
      showJoinScreenMessage.style.fontWeight = "bold";
      showJoinScreenMessage.style.marginTop = "20px";
      showJoinScreenMessage.style.marginLeft = "20px";
      $scope.participantGridContainer.appendChild(showJoinScreenMessage);
    }
  };

  $scope.handleMeetingEvents = function (meeting) {
    $scope.localParticipant = meeting.localParticipant;
    $scope.participants = meeting.participants;

    $scope.participantGridGenerator = function ({ participant }) {
      var participantGridItem = document.createElement("div");
      participantGridItem.style.backgroundColor = "lightgrey";
      participantGridItem.style.borderRadius = "10px";
      participantGridItem.style.height = "300px";
      participantGridItem.style.width = "320px";
      participantGridItem.style.marginTop = "8px";
      participantGridItem.style.display = "flex";
      participantGridItem.style.alignItems = "center";
      participantGridItem.style.justifyContent = "center";
      participantGridItem.style.position = "relative";
      participantGridItem.setAttribute(
        "id",
        `participant-grid-item-${participant.id}`
      );
      participantGridItem.setAttribute("class", "col-4");
      var participantMediaElement = document.createElement("div");
      participantMediaElement.setAttribute(
        "id",
        `participant-media-container-${participant.id}`
      );
      var nameElement = $scope.createNameElement(participant);
      $scope.participantGridContainer.appendChild(participantGridItem);
      participantGridItem.appendChild(participantMediaElement);
      participantMediaElement.appendChild(nameElement);
      var getParticipantMediaElement = document.getElementById(
        `participant-media-container-${participant.id}`
      );

      return {
        getParticipantMediaElement,
      };
    };

    if (meeting) {
      $scope.showJoinScreen = false;
      $scope.showMeetingScreen = true;
    }

    meeting.on("meeting-joined", function () {
      var showJoinScreenMessage = document.getElementById(
        "show-join-screen-message"
      );
      var topBar = document.getElementById("top-bar");
      showJoinScreenMessage.style.display = "none";
      topBar.style.display = "block";

      const { getParticipantMediaElement } = $scope.participantGridGenerator({
        participant: meeting.localParticipant,
      });

      meeting.localParticipant.on("stream-enabled", (stream) => {
        $scope.handleStreamEnabled(
          stream,
          meeting.localParticipant,
          true,
          getParticipantMediaElement
        );
      });
      meeting.localParticipant.on("stream-disabled", (stream) => {
        $scope.handleStreamDisabled(
          stream,
          meeting.localParticipant,
          true,
          getParticipantMediaElement
        );
      });
    });

    meeting.on("participant-left", (participant) => {
      var participantGridItem = document.getElementById(
        `participant-grid-item-${participant.id}`
      );
      $scope.participantGridContainer.removeChild(participantGridItem);
    });

    meeting.on("meeting-left", () => {
      // remove all children nodes from participant grid container
      while ($scope.participantGridContainer.firstChild) {
        $scope.participantGridContainer.removeChild(
          $scope.participantGridContainer.firstChild
        );
      }
      $scope.showMeetingScreen = false;
      $scope.showJoinScreen = true;
    });

    //remote participant
    meeting.on("participant-joined", (participant) => {
      var { getParticipantMediaElement } = $scope.participantGridGenerator({
        participant: participant,
      });
      participant.setQuality("high");
      participant.on("stream-enabled", (stream) => {
        $scope.handleStreamEnabled(
          stream,
          participant,
          false,
          getParticipantMediaElement
        );
      });
      participant.on("stream-disabled", (stream) => {
        $scope.handleStreamDisabled(
          stream,
          participant,
          false,
          getParticipantMediaElement
        );
      });
    });

    $scope.toggleWebcam = function () {
      if ($scope.isWebcamOn) {
        $scope.meeting.disableWebcam();
      } else {
        $scope.meeting.enableWebcam();
      }
      $scope.isWebcamOn = !$scope.isWebcamOn;
    };

    $scope.toggleMic = function () {
      if ($scope.isMicOn) {
        $scope.meeting.muteMic();
      } else {
        $scope.meeting.unmuteMic();
      }
      $scope.isMicOn = !$scope.isMicOn;
    };

    $scope.leaveMeeting = function () {
      $scope.meeting.leave();
      $scope.showMeetingScreen = false;
      $scope.showJoinScreen = true;
    };
  };
});
