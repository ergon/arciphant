package ch.ergon.arciphant.example.certificate.webapi

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("certificates")
interface CertificateController {

    @PostMapping
    fun issueCertificate(@RequestBody issueCertificate: IssueCertificateDto)

}
