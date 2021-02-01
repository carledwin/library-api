package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.constants.ConstantsError;
import com.carledwinti.library.api.dto.LoanDTO;
import com.carledwinti.library.api.dto.LoanFilterDTO;
import com.carledwinti.library.api.dto.ReturnedLoanDTO;
import com.carledwinti.library.api.exception.BusinessException;
import com.carledwinti.library.api.model.Book;
import com.carledwinti.library.api.model.Loan;
import com.carledwinti.library.api.service.BookService;
import com.carledwinti.library.api.service.LoanService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Arrays;
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

    @Test
    @DisplayName("Deve retornar um livro")
    public void returnBookTest() throws Exception {
        //scenario --returned:true
        ReturnedLoanDTO returnedLoanDTO = ReturnedLoanDTO.builder().returned(true).build();
        String returnedLoanDTOJson = new ObjectMapper().writeValueAsString(returnedLoanDTO);
        Long loanId = 1l;
        Loan loan = savedLoan();

        //mock
        // BDDMockito.given(loanService.getById(loanId)).willReturn(Optional.of(loan)); ou o de baixo
        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));
        //BDDMockito.given(loanService.update(savedLoan())).willReturn(Optional.of(loan)); ou o de baixo
        BDDMockito.given(loanService.update(Mockito.any(Loan.class))).willReturn(Optional.of(loan));

        //execution
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilders.patch(URL_LOAN_API.concat("/") + loanId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(returnedLoanDTOJson);

        //verification
        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(MockMvcResultMatchers.status().isOk());
        Mockito.verify(loanService, Mockito.times(1)).getById(loanId);
        Mockito.verify(loanService, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retornar um erro ao tentar retornar um livro inexistente")
    public void returnInexistentBookTest() throws Exception {
        //scenario --returned:true
        ReturnedLoanDTO returnedLoanDTO = ReturnedLoanDTO.builder().returned(true).build();
        String returnedLoanDTOJson = new ObjectMapper().writeValueAsString(returnedLoanDTO);
        Long loanId = 1l;

        //mock
        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        //execution
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilders.patch(URL_LOAN_API.concat("/") + loanId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnedLoanDTOJson);

        //verification
        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("errors").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value(ConstantsError.MSG_ERROR_RETURN_LOAN_ID_NOTFOUND));

        Mockito.verify(loanService, Mockito.times(1)).getById(loanId);
        Mockito.verify(loanService, Mockito.never()).update(null);
    }


    @Test
    @DisplayName("Deve filtrar loans")
    public void findLoanByFilter() throws Exception {
        //scenario
        Long loanId = 1l;
        Loan existentLoan = savedLoan();
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().isbn("").customer("").build();
        Loan loanFilter = Loan.builder().isbn("").customer("").build();
        int page=0, size=12, total=1;
        Page<Loan> loanPage = new PageImpl<Loan>(Arrays.asList(existentLoan), PageRequest.of(page, size), total);
        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=12", loanFilterDTO.getIsbn(), loanFilterDTO.getCustomer());

        //mock
        //BDDMockito.given(loanService.findByFilter(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class))).willReturn(loanPage);
        //ou abaixo
        BDDMockito.given(loanService.findByFilter(loanFilter, loanPage.getPageable())).willReturn(loanPage);

        //execution
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilders.get(URL_LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        //verification
        mockMvc.perform(mockHttpServletRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageNumber").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageSize").value(12))
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1));

        Mockito.verify(loanService, Mockito.times(1)).findByFilter(loanFilter, loanPage.getPageable());
    }

    private LoanDTO createNewLoanDTO(){
        return LoanDTO.builder().isbn("123").customer("Lariano").customerEmail("lariano@email.com").build();
    }

    private Loan savedLoan(){
        Book book = existentBook();
        return Loan.builder().id(1l)
                .isbn("123")
                .customer("Lariano")
                .customerEmail("lariano@email.com")
                .book(book)
                .returned(false)
                .loanDate(LocalDate.now())
                .build();
    }

    private Book existentBook(){
        return Book.builder().id(5678l).title("Antenor Santanará").isbn("123").author("Milanes").build();
    }
}
