# Personal AI
Not to be confused with the comercial offering on PlayStore

1. Go to [Google AI Studio](https://aistudio.google.com/).
2. Login with your Google account.
3. [Create](https://aistudio.google.com/app/apikey) an API key. Free at the time of writing this.
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


Features:
1. chat / text message mode
2. hands free Voice-to-Voice mode with natural sounding inflection. --Free APIs


TODO:
1. local directory and Google Drive upload / download integration
2. Model file ingestion and analysis
3. Multi-modal image, audio, and video generation
4. Model web searching and embedded link previewing (i.e. Youtube video)

