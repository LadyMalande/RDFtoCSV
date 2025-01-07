package com.miklosova.rdftocsvw.converter;

 import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
 import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
 import org.eclipse.rdf4j.repository.RepositoryConnection;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import static org.junit.jupiter.api.Assertions.*;
 import org.mockito.Mock;
 import static org.mockito.Mockito.*;
 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.eq;

class ConversionGatewayTest {

     private ConversionGateway conversionGateway;

     @Mock
     private IQueryParser mockQueryParser;

    @Mock
    private BasicQueryConverter basicQueryConverterMock;

    @Mock
    private SplitFilesQueryConverter splitQueryConverterMock;

     @Mock
     private RepositoryConnection mockRepositoryConnection;

     @BeforeEach
     void setUp() {
         conversionGateway = new ConversionGateway();
     }

     //BaseRock generated method id: ${testSetConversionMethod}, hash: 760F9509C9D97EDE13568893BCCA5D10
     @Test
     void testSetConversionMethod() {
         basicQueryConverterMock = mock(BasicQueryConverter.class);
         conversionGateway.setConversionMethod(basicQueryConverterMock);
         // We can't directly test the private field, so we'll test it indirectly
         // by calling processInput and verifying that the mock is called
         PrefinishedOutput<RowsAndKeys> mockOutput = mock(PrefinishedOutput.class);
         when(basicQueryConverterMock.convertWithQuery(any(RepositoryConnection.class))).thenReturn(mockOutput);
         PrefinishedOutput<RowsAndKeys> result = conversionGateway.processInput(mockRepositoryConnection);
         //assertSame(mockOutput, result);
         verify(basicQueryConverterMock).convertWithQuery(mockRepositoryConnection);
     }

     //BaseRock generated method id: ${testProcessInput}, hash: 5DCA877D3609DB89CED6287490F36495
     @Test
     void testProcessInput() {
         splitQueryConverterMock = mock(SplitFilesQueryConverter.class);
         conversionGateway.setConversionMethod(splitQueryConverterMock);
         PrefinishedOutput<RowsAndKeys> expectedOutput = mock(PrefinishedOutput.class);
         when(splitQueryConverterMock.convertWithQuery(mockRepositoryConnection)).thenReturn(expectedOutput);
         PrefinishedOutput<RowsAndKeys> result = conversionGateway.processInput(mockRepositoryConnection);
         assertSame(expectedOutput, result);
         verify(splitQueryConverterMock).convertWithQuery(mockRepositoryConnection);
     }

     //BaseRock generated method id: ${testProcessInputWithNullConversionMethod}, hash: 845F441EE0E20A455382C22FD5AC2514
     @Test
     void testProcessInputWithNullConversionMethod() {
         assertThrows(NullPointerException.class, () -> conversionGateway.processInput(mockRepositoryConnection));
     }
}