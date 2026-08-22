package ch.ergon.arciphant.example.certificate.domain

import ch.ergon.arciphant.example.exam.api.ExamResultIdFixtures
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CertificateServiceTest {

    private val certificateAuthorityMock = mock<CertificateAuthority>()
    private val certificateFileStoreMock = mock<CertificateFileStore>()
    private val certificateRepositoryMock = mock<CertificateRepository>()


    private val certificateService = CertificateService(
        certificateAuthority = certificateAuthorityMock,
        certificateFileStore = certificateFileStoreMock,
        certificateRepository = certificateRepositoryMock,
    )

    @Test
    fun `it should persist issued certificate`() {
        whenever(certificateAuthorityMock.issueCertificate(ExamResultIdFixtures.any)).thenReturn(CertificateFixtures.anyCertificate)

        certificateService.issueAndPersistCertificate(ExamResultIdFixtures.any)

        verify(certificateFileStoreMock)
            .persistCertificateDocument(CertificateFixtures.anyCertificate.id, CertificateFixtures.anyCertificate.document)
        verify(certificateRepositoryMock)
            .addCertificate(CertificateFixtures.anyCertificate)

    }

}
