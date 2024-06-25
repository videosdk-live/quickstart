import asyncio
from videosdk import MeetingConfig, VideoSDK
from meeting_events import MyMeetingEventHandler


VIDEOSDK_TOKEN="<TOKEN>"
MEETING_ID="<MEETING_ID>"
NAME="Dr. A. P. J. Abdul Kalam"

loop = asyncio.get_event_loop()

def main():
  try:
    meeting_config = MeetingConfig(meeting_id=MEETING_ID, name=NAME, mic_enabled=True, webcam_enabled=True, token=VIDEOSDK_TOKEN)

    meeting = VideoSDK.init_meeting(**meeting_config)
    
    meeting.add_event_listener(MyMeetingEventHandler())

    print("joining into meeting...")

    meeting.join()

    print("joined successfully")
  except KeyboardInterrupt:
    print("releasing meeting instance")
    meeting.release()

if __name__ == '__main__':
  main()
  loop.run_forever()
