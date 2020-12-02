import com.tutuka.reconciliation.Application;
import com.tutuka.reconciliation.trxcompare.data.Transaction;
import com.tutuka.reconciliation.infrastructure.exception.StorageFileNotFoundException;
import com.tutuka.reconciliation.trxcompare.util.TransactionUtiltiy;
import com.tutuka.reconciliation.trxcompare.service.CsvReaderService;
import com.tutuka.reconciliation.trxcompare.service.FileSystemStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = Application.class)
public class FileuploadFormTests {

	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private TestRestTemplate restTemplate;

	@MockBean
	private FileSystemStorageService storageService;

	@LocalServerPort
	private int port;

	@Test
	public void shouldDownloadFile() throws Exception {
		ClassPathResource resource = new ClassPathResource("testupload.txt", getClass());
		given(this.storageService.loadAsResource("testupload.txt")).willReturn(resource);

		ResponseEntity<String> response = this.restTemplate
				.getForEntity("/api/files/{filename}", String.class, "testupload.txt");

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
				.isEqualTo("attachment; filename=\"testupload.txt\"");
		assertThat(response.getBody()).isEqualTo("Test Upload");
	}

	private List<Transaction> loadFile(String filename) throws Exception {
		File testFile = new File(getClass().getClassLoader().getResource(filename).getFile());
		CsvReaderService csvBeanReader = new CsvReaderService();
		List<Transaction> transactionsList = new ArrayList<Transaction>();
		transactionsList = csvBeanReader.csvRead(testFile.getAbsolutePath());
		return transactionsList;
	}

	@Test
	public void cSVBeanReadTest() throws Exception {
		Iterator<Transaction> testIter = loadFile("ValidFile.csv").iterator();
		assertThat(testIter.next().getWalletReference()).isEqualTo("P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5");
	}

	@Test
	public void removeduplicateTransactionTest() throws Exception {
		Iterator<Transaction> testIter = new TransactionUtiltiy().removeDuplicates(loadFile("DuplicateFile.csv"))
				.iterator();
		assertThat((testIter.next()).getTransactionID().toString()).isEqualTo("584011808649511");
	}
	
	@Test
	public void removeduplicatesFromListTest() throws Exception {
		assertThat(String.valueOf(new TransactionUtiltiy().removeDuplicates(loadFile("DuplicateFile.csv")).size())).isEqualTo("2");
	}
	
    @SuppressWarnings("unchecked")
    @Test
    public void should404WhenMissingFile() throws Exception {
        given(this.storageService.loadAsResource("test.txt"))
                .willThrow(StorageFileNotFoundException.class);

        this.mvc.perform(get("api/files/test.txt")).andExpect(status().isNotFound());
    }
    
    //Test for handling headers not in order and extra headers
    @Test
    public void handleExtraHeadersTest() {
    	Iterator<Transaction> testIter = null;
		try {
			testIter = loadFile("handleextraHeadersTest.csv").iterator();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertThat(testIter.next().getTransactionID().toString()).isEqualTo("584012370494730");
    }
}