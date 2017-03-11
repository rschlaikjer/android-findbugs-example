# Example FindBugs detector for Android

This repo contains example code to accompany
[this writeup](http://localhost:1313/post/custom-android-findbugs/)
about implementing custom findbugs plugins for android projects.

The demo project here defines a database with some leaky and non-leaky
accessors, as well as a detector that will raise warnings when Android cursors
are not freed using a try-with-resources clause.
