<a><img width="1660" height="412" alt="timecapsule-up" src="https://github.com/user-attachments/assets/433a0b7b-9788-479c-90e6-5b48ec000e70" /></a>

# TimeCapsule Lambda code <br> [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/ita-social-projects/GreenCity/blob/master/LICENSE) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=timecapsule&metric=coverage)](https://sonarcloud.io/dashboard?id=timecapsule) [![GitHub release](https://img.shields.io/static/v1?label=Pre-release&message=v.1.0.0&color=yellowgreen)](https://github.com/DanHorbatiuk/timecapsule/releases)

**Copyright 2025 Danylo Horbatiuk**

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

<br>

> [!IMPORTANT]
>   <b>To add Lambda function code </b> `sendCapsuleOpen` <b> you need: </b>
> 
>   * `New Project` -> `Project From Version Control` -> `Repository URL` -> `URL` (https://github.com/DanHorbatiuk/timecapsule-lambda.git) -> `Clone`.
>   * Open `Terminal` write `mvn clean package`
>   * Get `/timecapsule/target/timecapsule-1.0.0-shaded.jar` and upload this jar in S3 storage
>   * In Lambda `sendCapsuleOpen` set function -> `File from S3 storage`
