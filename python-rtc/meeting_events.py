from videosdk import Participant, MeetingEventHandler
from participant_events import MyParticipantEventHandler


class MyMeetingEventHandler(MeetingEventHandler):
    def __init__(self):
        super().__init__()

    def on_error(self, data):
        print("Meeting Error: ", data)

    def on_meeting_joined(self, data):
        print("Meeting joined")

    def on_meeting_left(self, data):
        print("Meeting left")

    def on_participant_joined(self, participant: Participant):
        print("Participant joined:", participant.display_name)
        participant.add_event_listener(
            MyParticipantEventHandler(participant_id=participant.id))

    def on_participant_left(self, participant):
        print("Participant left:", participant)
