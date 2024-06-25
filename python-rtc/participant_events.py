from videosdk import ParticipantEventHandler, Stream

class MyParticipantEventHandler(ParticipantEventHandler):
  def __init__(self, participant_id:str):
    super().__init__()
    self.participant_id = participant_id
  def on_stream_enabled(self, stream: Stream):
    print("paricipant :: stream enabled", self.participant_id, stream.kind)

  def on_stream_disabled(self, stream: Stream):
    print("paricipant :: stream disabled", self.participant_id, stream.kind)

  def on_media_status_changed(self, data):
    print("paricipant :: media status changed", self.participant_id, data)

  def on_video_quality_changed(self, data):
    print("paricipant :: video quality changed", self.participant_id, data)
