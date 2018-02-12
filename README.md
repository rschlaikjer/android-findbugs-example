# Example FindBugs detector for Android

This repo contains example code to accompany
[this writeup](https://rhye.org/post/custom-android-findbugs/)
about implementing custom findbugs plugins for android projects.

The demo project here defines a database with some leaky and non-leaky
accessors, as well as a detector that will raise warnings when Android cursors
are not freed using a try-with-resources clause.

# License

Copyright 2018 Ross Schlaikjer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
