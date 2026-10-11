package com.devlink.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class DevlinkBackendApplication

fun main(args: Array<String>) {
	runApplication<DevlinkBackendApplication>(*args)
}
