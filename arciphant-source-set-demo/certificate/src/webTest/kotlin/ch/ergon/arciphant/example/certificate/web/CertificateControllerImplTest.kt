package ch.ergon.arciphant.example.certificate.web

import ch.ergon.arciphant.example.certificate.domain.CertificateService
import ch.ergon.arciphant.example.certificate.webapi.IssueCertificateDto
import ch.ergon.arciphant.example.exam.api.ExamResultIdFixtures
import ch.ergon.arciphant.example.shared.web.IsolatedWebMvcTest
import ch.ergon.arciphant.example.shared.web.postWithRequestBody
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc

@IsolatedWebMvcTest(CertificateControllerImpl::class)
class CertificateControllerImplTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var certificateServiceMock: CertificateService

    @Test
    fun `it should issue a certificate`() {
        val examResultId = ExamResultIdFixtures.any
        val body = IssueCertificateDto(examResultId = examResultId.value)

        mockMvc.postWithRequestBody("/certificates", body)
            .andExpect { status { isOk() } }
    }

}
