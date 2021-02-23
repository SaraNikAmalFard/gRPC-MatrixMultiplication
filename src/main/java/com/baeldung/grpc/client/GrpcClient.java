package com.baeldung.grpc.client;

import com.grpc.MatrixMultiplicationRequest;
import com.grpc.MatrixMultiplicationReply;
import com.grpc.CalculatorServiceGrpc;
import com.grpc.Row;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class GrpcClient {

    public static int MAX = 4;

    public static MatrixMultiplicationReply replyBuilder(int C[][]) {
        MatrixMultiplicationReply.Builder reply = MatrixMultiplicationReply.newBuilder();
        for (int i = 0; i < MAX; i++) {
            Row.Builder row = Row.newBuilder();
            for (int j = 0; j < C.length; j++) {
                row.addColumn(C[i][j]);
                row.setColumn(j,C[i][j]);
            }
            reply.addMatrixC(i, row);
        }
        return reply.build();
    }

    public static MatrixMultiplicationRequest requestBuilder(int A[][], int B[][]) {
        MatrixMultiplicationRequest.Builder request = MatrixMultiplicationRequest.newBuilder();
        for (int i = 0; i < MAX; i++) {
            Row.Builder row = Row.newBuilder();
            for (int col : A[i]) {
                row.addColumn(col);

            }
            request.addMatrixA(row.build());
        }

        for (int i = 0; i < MAX; i++) {
            Row.Builder row = Row.newBuilder();
            for (int col : B[i]) {
                row.addColumn(col);
            }
            request.addMatrixB(row.build());
        }
        return request.build();
    }

    public static int[][] arrayReplyBuilder(MatrixMultiplicationReply reply) {
        int[][] C = new int[MAX][MAX];
        for (int i = 0; i < MAX; i++) {
            for (int j = 0; j < C[i].length; j++) {
                C[i][j] = reply.getMatrixC(i).getColumn(j);
            }
        }
        return C;
    }

    public static MatrixMultiplicationReply multiplyMatrixBlock(int[][] A, int[][] B) {
        MatrixMultiplicationRequest request = requestBuilder(A, B);
        int bSize = 2;
        int[][] A1 = new int[MAX][MAX];
        int[][] A2 = new int[MAX][MAX];
        int[][] A3 = new int[MAX][MAX];
        int[][] B1 = new int[MAX][MAX];
        int[][] B2 = new int[MAX][MAX];
        int[][] B3 = new int[MAX][MAX];
        int[][] C1 = new int[MAX][MAX];
        int[][] C2 = new int[MAX][MAX];
        int[][] C3 = new int[MAX][MAX];
        int[][] D1 = new int[MAX][MAX];
        int[][] D2 = new int[MAX][MAX];
        int[][] D3 = new int[MAX][MAX];
        int[][] res = new int[MAX][MAX];

        for (int i = 0; i < bSize; i++) {
            for (int j = 0; j < bSize; j++) {
                A1[i][j] = request.getMatrixA(i).getColumn(j);
                B1[i][j] = request.getMatrixB(i).getColumn(j);
            }
        }

        for (int i = 0; i < bSize; i++) {
            for (int j = bSize; j < MAX; j++) {
                B1[i][j - bSize] = request.getMatrixA(i).getColumn(j);
                B2[i][j - bSize] = request.getMatrixB(i).getColumn(j);
            }
        }

        for (int i = bSize; i < MAX; i++) {
            for (int j = 0; j < bSize; j++) {
                C1[i - bSize][j] = request.getMatrixA(i).getColumn(j);
                C2[i - bSize][j] = request.getMatrixB(i).getColumn(j);
            }
        }
        for (int i = bSize; i < MAX; i++) {
            for (int j = bSize; j < MAX; j++) {
                D1[i - bSize][j - bSize] = request.getMatrixA(i).getColumn(j);
                D2[i - bSize][j - bSize] = request.getMatrixB(i).getColumn(j);
            }
        }
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
                .usePlaintext()
                .build();
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub
                = CalculatorServiceGrpc.newBlockingStub(channel);

        MatrixMultiplicationRequest req1 = requestBuilder(A1, A2);
        MatrixMultiplicationRequest req2 = requestBuilder(B1, C2);
        MatrixMultiplicationRequest req3 = requestBuilder(A1, B2);
        MatrixMultiplicationRequest req4 = requestBuilder(B1, D2);
        MatrixMultiplicationRequest req5 = requestBuilder(C1, A2);
        MatrixMultiplicationRequest req6 = requestBuilder(D1, C2);
        MatrixMultiplicationRequest req7 = requestBuilder(C1, B2);
        MatrixMultiplicationRequest req8 = requestBuilder(D1, D2);

        MatrixMultiplicationReply rep1 = stub.multiplyBlock(req1); //A1,A2
        MatrixMultiplicationReply rep2 = stub.multiplyBlock(req2); //B1,C2
        MatrixMultiplicationReply rep3 = stub.multiplyBlock(req3); //A1,B2
        MatrixMultiplicationReply rep4 = stub.multiplyBlock(req4); //B1,D2
        MatrixMultiplicationReply rep5 = stub.multiplyBlock(req5); //C1,A2
        MatrixMultiplicationReply rep6 = stub.multiplyBlock(req6); //D1,C2
        MatrixMultiplicationReply rep7 = stub.multiplyBlock(req7); //C1,B2
        MatrixMultiplicationReply rep8 = stub.multiplyBlock(req8); //D1,D2

        MatrixMultiplicationRequest request1 = requestBuilder(arrayReplyBuilder(rep1), arrayReplyBuilder(rep2));
        MatrixMultiplicationRequest request2 = requestBuilder(arrayReplyBuilder(rep3), arrayReplyBuilder(rep4));
        MatrixMultiplicationRequest request3 = requestBuilder(arrayReplyBuilder(rep5), arrayReplyBuilder(rep6));
        MatrixMultiplicationRequest request4 = requestBuilder(arrayReplyBuilder(rep7), arrayReplyBuilder(rep8));

        A3 = arrayReplyBuilder(stub.addBlock(request1));
        B3 = arrayReplyBuilder(stub.addBlock(request2));
        C3 = arrayReplyBuilder(stub.addBlock(request3));
        D3 = arrayReplyBuilder(stub.addBlock(request4));

        for (int i = 0; i < bSize; i++) {
            for (int j = 0; j < bSize; j++) {
                res[i][j] = A3[i][j];
            }
        }
        for (int i = 0; i < bSize; i++) {
            for (int j = bSize; j < MAX; j++) {
                res[i][j] = B3[i][j - bSize];
            }
        }
        for (int i = bSize; i < MAX; i++) {
            for (int j = 0; j < bSize; j++) {
                res[i][j] = C3[i - bSize][j];
            }
        }
        for (int i = bSize; i < MAX; i++) {
            for (int j = bSize; j < MAX; j++) {
                res[i][j] = D3[i - bSize][j - bSize];
            }
        }
        for (int i = 0; i < MAX; i++) {
            for (int j = 0; j < MAX; j++) {
                System.out.print(res[i][j] + " ");
            }
            System.out.println("");
        }
        channel.shutdown();
        return replyBuilder(res);
    }

    public static void main(String[] args) throws InterruptedException {
        int A[][] = {{1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}};

        int B[][] = {{1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}};

        MatrixMultiplicationReply response = multiplyMatrixBlock(A, B);
    }
}
