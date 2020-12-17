# crypto
Small java crypto library

## Recovery codes (recoverycode package)
- IterativeCode
- HammingCode
- ModifiedHammingCode
- CycleCode

## Interleaving (interleaving package)
- BlockInterleaving

## Entropy (entropy package)
- EntropyUtil

## Compress (compress package)
- ArithmeticEncoding
- ArithmeticEncodingDecimal
- ShannonFanoCode
- LempelZivCompress

## Util (util package)
- MatrixUtil
- PrintUtil

## Alphabet (alphabet package)
- Alphabet

## Blueprint
Just pass the arguments and get the answer as a report to pointed IOStream

## Examples

```java
// some code ...
HammingCode hammingCode = new HammindCode(someOutputStream)
int[][] checkMatrix = hammingCode.getCheckMatrix(numberOfRedundantBytes, numberOfRedundantBytes);
int[] encodedMessage = hammingCode.encode(checkMatrix, someMessage, numberOfRedundantBytes, numberOfRedundantBytes);
// sending and receiving encoded message ...
int[] calculatedRedundantBytes = hammingCode.calculateRedundantBytes(checkMatrix, encodedMessage, numberOfInformationBytes, numberOfRedundantBytes);
int[] receivedRedundantBytes = hammingCode.getRedundantBytes(encodedMessage, numberOfInformationBytes, numberOfRedundantBytes);
int[] syndrome = hammingCode.getSyndrome(receivedRedundantBytes, calculatedRedundantBytes);
int[] recoveryBytes = hammingCode.getRecoveryBytes(syndrome, checkMatrix, numberOfInformationBytes, numberOfRedundantBytes);
int[] recoveredMessage = hammingCode.recoverMessage(message, recoveryBytes);
// some code ...
```

### How to install

Comming soon...

### Contributors
* [zadorozhn](https://github.com/ZadorozhN)
* [Ivanshka](https://github.com/Ivanshka)
* [BadLiar37](https://github.com/BadLiar37)
