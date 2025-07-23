
## Ktor
* Request -> Routing -> Plugin -> Handler -> Plugin -> Response

### kotlin
`suspend` : 코루틴에서 많이 사용하는 함수(중단 함수)
- ktor 에서 비동기 처리를 위해 사용
- suspend 함수는 다른 suspend 함수나, 코루틴 빌더 안에서만 호출 가능

`inline` : 호출부에서 코드 자체가 복사가 되어 실행되는 함수
- 성능을 높이기 위해 사용
- inline 함수는 다른 inline 함수나, 람다식 안에서만 호출 가능
- 람다식이 inline 함수의 인자로 전달되면, 람다식의 코드가 inline 함수의 호출부에 삽입되어 실행됨

