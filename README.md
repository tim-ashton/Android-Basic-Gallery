# Android-Basic-Gallery

Android Basic Galler is a simple but functional photo gallery that uses a Cursor Loader to with an AsycTask to load images and a Cursor Adapter to display the images in a GridView.

I have taken the base implementation from this presentation by Jeff Sharkey "Doing More with Less" although it is has been substantially modified from what is in the presentation.

The LRU cache used in this gallery is the cache introduced in API 17 so a custom one (as per the presentation) is no longer required. I have added a retained fragment to keep the LRU cache alive across configuration changes.
