import asyncio
import os
from videosdk import MeetingConfig, VideoSDK
from meeting_events import MyMeetingEventHandler
import dotenv
dotenv.load_dotenv()

VIDEOSDK_TOKEN = os.getenv("VIDEOSDK_TOKEN")
MEETING_ID = os.getenv("MEETING_ID")
NAME = "VideoSDK Python"

loop = asyncio.get_event_loop()


def main():

    meeting_config = MeetingConfig(
        meeting_id=MEETING_ID, name=NAME, mic_enabled=True, webcam_enabled=True, token=VIDEOSDK_TOKEN)

    meeting = VideoSDK.init_meeting(**meeting_config)

    meeting.add_event_listener(MyMeetingEventHandler())

    print("joining into meeting...")

    meeting.join()

    print("joined successfully")


if __name__ == '__main__':
    main()
    loop.run_forever()
