# How to get the actual .apk file

I can't compile the code myself (no internet/Android SDK in my environment), but this
folder now has everything set up so **GitHub builds the APK for you, for free, in the
cloud.** You just need a free GitHub account. Takes about 10 minutes total, no coding.

## Steps

1. **Create a free GitHub account** at https://github.com/join (skip if you have one).

2. **Create a new empty repository**
   - Go to https://github.com/new
   - Name it anything, e.g. `deskmint-dashboard`
   - Leave it "Public" or "Private", doesn't matter
   - Don't check any of the "initialize with" boxes
   - Click **Create repository**

3. **Upload this folder's contents to that repository**
   - On the new repo's page, click **"uploading an existing file"**
   - Unzip `DeskMint-Dashboard-Project.zip` on your computer first
   - Drag the *contents* of the `DeskMint` folder (not the folder itself — the
     `app`, `.github`, `build.gradle`, `settings.gradle` etc. should be at the top
     level of the repo) into the upload box
   - Scroll down, click **Commit changes**

4. **Watch it build**
   - Click the **Actions** tab at the top of your repo
   - You'll see a workflow run called "Build DeskMint APK" — click it
   - It takes a few minutes. A green checkmark means it succeeded.
   - If it's red/failed, click into the run and read the error — most likely cause
     is a small typo somewhere in the Java, since none of this was ever compiled
     before now. Paste the error back to me and I'll fix the code.

5. **Download the APK**
   - On that same completed run's page, scroll down to **Artifacts**
   - Click **DeskMint-debug-apk** to download a zip containing `app-debug.apk`

6. **Install it on the Lenovo tablet**
   - Copy `app-debug.apk` onto the tablet (USB cable, or a file-sharing app)
   - On the tablet: Settings → Security → enable **Unknown sources**
   - Open the APK file with a file manager app and tap **Install**

## After that

Every time you push a code change to the repo, GitHub automatically rebuilds the APK —
just grab the new one from the Actions tab.

If you'd rather not deal with GitHub at all, the other option is installing Android
Studio on a Windows/Mac/Linux computer and building it locally (free, but a bigger
download). Let me know if you'd prefer instructions for that instead.
