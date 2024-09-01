# Personal AI

1. Go to [Google AI Studio](https://aistudio.google.com/).
2. Login with your Google account.
3. [Create](https://aistudio.google.com/app/apikey) an API key. Free at the time of writing this. Note that in Europe the free tier may not be available.
4. Check out this repository. `git clone https://github.com/lllsondowlll/PersonalAi-Android`
5. Open and build the project folder of this repo in [Android Studio](https://developer.android.com/studio).
6. Paste your API key into the`local.properties` file under the populated `sdk.dir` like so: 
```
sdk.dir=/path/to/your/android_studio
apiKey=YOUR_API_KEY_HERE
```
7. Run the app. Note: Anything typed in square brackets [You bark like a dog] will override the model's system instructions dynamically.
8. For detailed instructions, try the
[Android SDK tutorial](https://ai.google.dev/tutorials/android_quickstart) on [ai.google.dev](https://ai.google.dev).
