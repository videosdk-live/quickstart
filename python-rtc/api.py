import requests

# This is the Auth token, you will use it to generate a meeting and connect to it
token = "<TOKEN>"

# API call to create a meeting
def create_meeting(token):
    url = "https://api.videosdk.live/v2/rooms"
    headers = {
        "authorization": token,
        "Content-Type": "application/json"
    }
    response = requests.post(url, headers=headers, json={})
    response_data = response.json()
    room_id = response_data.get("roomId")
    return room_id

# Example usage
meeting_id = create_meeting(token)
print("Meeting ID:", meeting_id)
