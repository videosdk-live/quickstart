from videosdk import Participant, MeetingEventHandler
from participant_events import MyParticipantEventHandler

class MyMeetingEventHandler(MeetingEventHandler):
  def __init__(self):
      super().__init__()

  def on_error(self, data):
    print("Error :: ", data)

  def on_meeting_joined(self, data):
    print("meeting :: joined ", data)

  def on_meeting_left(self, data):
    print("meeting :: left ", data)

  def on_meeting_state_change(self, data):
    print("meeting :: state changed ", data)

  def on_participant_joined(self, participant: Participant):
    print("participant joined ", participant)
    participant.add_event_listener(MyParticipantEventHandler(participant_id=participant.id))

  def on_participant_left(self, participant):
    print("participant left ", participant)

  def on_speaker_changed(self, data):
    print("Meeting :: speaker changed ", data)
