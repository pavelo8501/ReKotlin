package po.test.exposify.setup.mocks

import po.auth.extensions.generatePassword
import po.test.exposify.setup.dtos.User

val mockedUser : User get() {
    return User(
        id = 0,
        login = "some_login",
        hashedPassword = generatePassword("password"),
        name = "name",
        email = "nomail@void.null"
    )
}

