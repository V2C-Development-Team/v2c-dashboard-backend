# V2C Dashboard Backend

*Copyright (c) 2020 V2C Development Team. All rights reserved.*

## Build

You need Java 11. This project can be tested and compiled with the following command.

`gradlew clean shadowJar`

## Execution

To run it, just do `java -jar build\libs\v2c-dispatcher.jar`.

Also, make sure you follow the docs [here](https://docs.google.com/document/d/1hD70BRmOZiTVyRUzjs6cW6FZHCFKZOaX_qHrvPKSbWI/edit?usp=sharing) so that the tunnel can be set up or whatever.

You can optionally specify some command-line arguments.

|Short Param|Long Param|Description                                         |Default                             |
|:----------|:---------|:---------------------------------------------------|:-----------------------------------|
|-d         |--database|Specifies the target database server.               |127.0.0.1:27017                     |
|-k         |--preshared-key|Specifies the preshared key for authentication.|484dd6d1-9262-4975-a707-4238e08ed266|
|-p         |--port    |Specifies the server's listening port.              |2586                                |

You should definitely change the security-related options.

## License

**This repository is subject to the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).**
