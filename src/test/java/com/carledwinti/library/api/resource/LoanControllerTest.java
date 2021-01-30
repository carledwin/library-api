package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.dto.LoanDTO;
import com.carledwinti.library.api.exception.BusinessException;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.model.Loan;
import com.carledwinti.library.api.service.BookService;
import com.carledwinti.library.api.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = {LoanController.class})
@AutoConfigureMockMvc
public class LoanControllerTest {

    static String URL_LOAN_API = "/api/loans";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookService bookService;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("Deve realizar um empréstimo")
    public void createLoan() throws Exception {
        //scenario
        String isbn = "123";
        LoanDTO loanDTO = createNewLoanDTO();
        Loan savedLoan = savedLoan();
        String loanStringJson = new ObjectMapper().writeValueAsString(loanDTO);

        //mock
        BDDMockito.given(bookService.getBookByIsbn(isbn)).willReturn(Optional.of(existentBook()));
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(Optional.of(savedLoan));

        //execution
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(URL_LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loanStringJson);


        //MockMvcResultMatchers.jsonPath("customer") //para validar um Json de retorno
        //MockMvcResultMatchers.content().string("1")//para validar uma String de retorno

        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(savedLoan.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(savedLoan.getIsbn()))
                .andExpect(MockMvcResultMatchers.jsonPath("customer").value(savedLoan.getCustomer()))
                .andExpect(MockMvcResultMatchers.jsonPath("returned").isBoolean());
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar realizar emprestimo de livro inexistnte")
    public void isbnNotExists() throws Exception {
        //scenario
        String isbn = "123";
        LoanDTO loanDTO = createNewLoanDTO();
        Loan savedLoan = savedLoan();
        String loanStringJson = new ObjectMapper().writeValueAsString(loanDTO);

        //mock
        BDDMockito.given(bookService.getBookByIsbn(isbn)).willReturn(Optional.empty());

        //execution
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(URL_LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loanStringJson);

        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]")
                        .value(ConstantsError.MSG_ERROR_BOOK_NOT_FOUND_FOR_ISBN));

        Mockito.verify(bookService, Mockito.times(1)).getBookByIsbn(isbn);
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar realizar emprestimo de livro emprestado")
    public void loanedBookToLoan() throws Exception {
        //scenario
        String isbn = "123";
        LoanDTO loanDTO = createNewLoanDTO();
        Loan savedLoan = savedLoan();
        String loanStringJson = new ObjectMapper().writeValueAsString(loanDTO);

        //mock
        BDDMockito.given(bookService.getBookByIsbn(isbn)).willReturn(Optional.of(existentBook()));
        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException(ConstantsError.MSG_ERROR_BOOK_ALREADY_LOANED));

        //execution
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(URL_LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loanStringJson);

        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]")
                        .value(ConstantsError.MSG_ERROR_BOOK_ALREADY_LOANED));

        Mockito.verify(bookService, Mockito.times(1)).getBookByIsbn(isbn);
    }

    private LoanDTO createNewLoanDTO(){
        return LoanDTO.builder().isbn("123").customer("Lariano").build();
    }

    private Loan savedLoan(){
        Book book = existentBook();
        return Loan.builder().id(1l)
                .isbn("123")
                .customer("Lariano")
                .book(book)
                .returned(false)
                .loanDate(LocalDate.now())
                .build();
    }

    private Book existentBook(){
        return Book.builder().id(5678l).title("Antenor Santanará").isbn("123").author("Milanes").build();
    }
}
