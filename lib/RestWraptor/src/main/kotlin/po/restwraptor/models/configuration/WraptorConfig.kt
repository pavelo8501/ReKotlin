package po.restwraptor.models.configuration

import po.restwraptor.enums.EnvironmentType

class WraptorConfig(
    val enviromnent : EnvironmentType = EnvironmentType.BUILD,
    val apiConfig: ApiConfig = ApiConfig(),
    val authConfig : AuthConfig = AuthConfig()
) {


}