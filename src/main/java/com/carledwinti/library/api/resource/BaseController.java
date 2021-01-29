package com.carledwinti.library.api.resource;

import com.carledwinti.library.api.exception.ApiErrors;
import com.carledwinti.library.api.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

public class BaseController {
    //aqui vamos utilizar o ExceptionHandler para tratar exceptions da nossa api
    //essa exception ApiErros automaticamente será lançada toda vez que o @Valid tentar validar uma request e ela não for valida,
    //pois ao criar um method anotado com @ExceptionHandler ele irá interceptar o @Valid e capturara o erro
    //mas, somente isso não basta, agora ele irá retornar a lista de errors, mas irá com ResponseStatus default do path, neste caso 200.
    // Como precisaremos de um retorno 400 BadReques também precisamos anotar o método handleValidationExceptions com @ReponseStatus
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException methodArgumentNotValidException){

        //será recebido como parametro o retorno da exception com um BindResult que contém todas as mensagens de erros
        BindingResult bindingResult = methodArgumentNotValidException.getBindingResult();

        //contém todos os erros que ocorreram na validação
        List<ObjectError> objectErrors = bindingResult.getAllErrors();

        return new ApiErrors(bindingResult);

    }

    //agora podemos criar um ExceptionHandler para uma exception específica, neste caso BusinessException
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessExceptions(BusinessException businessException){
        return new ApiErrors(businessException);
    }
}
