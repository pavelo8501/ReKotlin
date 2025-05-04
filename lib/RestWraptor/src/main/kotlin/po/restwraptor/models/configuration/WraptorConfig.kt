package po.restwraptor.models.configuration

import po.restwraptor.enums.EnvironmentType

class WraptorConfig(
    var apiConfig: ApiConfig = ApiConfig(),
    var authConfig : AuthConfig = AuthConfig()
)