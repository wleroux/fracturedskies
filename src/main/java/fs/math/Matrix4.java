package fs.math;

import java.nio.FloatBuffer;

import static java.lang.String.format;

public final class Matrix4 {

    private float m00;
    private float m01;
    private float m02;
    private float m03;
    private float m10;
    private float m11;
    private float m12;
    private float m13;
    private float m20;
    private float m21;
    private float m22;
    private float m23;
    private float m30;
    private float m31;
    private float m32;
    private float m33;

    public Matrix4(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    ) {
        this.m00 = m00; this.m01 = m01; this.m02 = m02; this.m03 = m03;
        this.m10 = m10; this.m11 = m11; this.m12 = m12; this.m13 = m13;
        this.m20 = m20; this.m21 = m21; this.m22 = m22; this.m23 = m23;
        this.m30 = m30; this.m31 = m31; this.m32 = m32; this.m33 = m33;
    }

    public static Matrix4 mat4(Matrix4 m) {
        return new Matrix4(
                m.m00, m.m01, m.m02, m.m03,
                m.m10, m.m11, m.m12, m.m13,
                m.m20, m.m21, m.m22, m.m23,
                m.m30, m.m31, m.m32, m.m33
        );
    }


    public static Matrix4 mat4(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    ) {
        return new Matrix4(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        );
    }

    public static Matrix4 mat4(Vector3 position) {
        return mat4(
                1, 0, 0, position.x(),
                0, 1, 0, position.y(),
                0, 0, 1, position.z(),
                0, 0, 0, 1
        );
    }

    public static Matrix4 mat4(Vector3 position, Quaternion4 rotation) {
        float xx      = rotation.x() * rotation.x();
        float xy      = rotation.x() * rotation.y();
        float xz      = rotation.x() * rotation.z();
        float xw      = rotation.x() * rotation.w();

        float yy      = rotation.y() * rotation.y();
        float yz      = rotation.y() * rotation.z();
        float yw      = rotation.y() * rotation.w();

        float zz      = rotation.z() * rotation.z();
        float zw      = rotation.z() * rotation.w();

        return mat4(
                1 - 2 * ( yy + zz ),     2 * ( xy - zw ),     2 * ( xz + yw ), position.x(),
                    2 * ( xy + zw ), 1 - 2 * ( xx + zz ),     2 * ( yz - xw ), position.y(),
                    2 * ( xz - yw ),     2 * ( yz + xw ), 1 - 2 * ( xx + yy ), position.z(),
                                  0,                   0,                   0, 1
        );
    }

    public static Matrix4 mat4(Quaternion4 rotation) {
        float xx      = rotation.x() * rotation.x();
        float xy      = rotation.x() * rotation.y();
        float xz      = rotation.x() * rotation.z();
        float xw      = rotation.x() * rotation.w();

        float yy      = rotation.y() * rotation.y();
        float yz      = rotation.y() * rotation.z();
        float yw      = rotation.y() * rotation.w();

        float zz      = rotation.z() * rotation.z();
        float zw      = rotation.z() * rotation.w();

        return mat4(
                1 - 2 * ( yy + zz ),     2 * ( xy - zw ),     2 * ( xz + yw ), 0,
                    2 * ( xy + zw ), 1 - 2 * ( xx + zz ),     2 * ( yz - xw ), 0,
                    2 * ( xz - yw ),     2 * ( yz + xw ), 1 - 2 * ( xx + yy ), 0,
                                  0,                   0,                   0, 1
                );
    }


    public static Matrix4 perspective(float fov, int width, int height, float near, float far) {
        float angle = (float) Math.tan(fov / 2);
        float aspect = (float) width / (float) height;

        return mat4(
            1f / (aspect * angle),         0,                            0,                                0,
                                0, 1 / angle,                            0,                                0,
                                0,         0, (-near - far) / (near - far), (2 * near * far) / (near - far),
                                0,         0,                            1,                                0
        );
    }


    public float m00() {
        return m00;
    }

    public float m01() {
        return m01;
    }

    public float m02() {
        return m02;
    }

    public float m03() {
        return m03;
    }

    public float m10() {
        return m10;
    }

    public float m11() {
        return m11;
    }

    public float m12() {
        return m12;
    }

    public float m13() {
        return m13;
    }

    public float m20() {
        return m20;
    }

    public float m21() {
        return m21;
    }

    public float m22() {
        return m22;
    }

    public float m23() {
        return m23;
    }

    public float m30() {
        return m30;
    }

    public float m31() {
        return m31;
    }

    public float m32() {
        return m32;
    }

    public float m33() {
        return m33;
    }


    public Matrix4 m00(float m00) {
        this.m00 = m00;
        return this;
    }

    public Matrix4 m01(float m01) {
        this.m01 = m01;
        return this;
    }

    public Matrix4 m02(float m02) {
        this.m02 = m02;
        return this;
    }

    public Matrix4 m03(float m03) {
        this.m03 = m03;
        return this;
    }

    public Matrix4 m10(float m10) {
        this.m10 = m10;
        return this;
    }

    public Matrix4 m11(float m11) {
        this.m11 = m11;
        return this;
    }

    public Matrix4 m12(float m12) {
        this.m12 = m12;
        return this;
    }

    public Matrix4 m13(float m13) {
        this.m13 = m13;
        return this;
    }

    public Matrix4 m20(float m20) {
        this.m20 = m20;
        return this;
    }

    public Matrix4 m21(float m21) {
        this.m21 = m21;
        return this;
    }

    public Matrix4 m22(float m22) {
        this.m22 = m22;
        return this;
    }

    public Matrix4 m23(float m23) {
        this.m23 = m23;
        return this;
    }

    public Matrix4 m30(float m30) {
        this.m30 = m30;
        return this;
    }

    public Matrix4 m31(float m31) {
        this.m31 = m31;
        return this;
    }

    public Matrix4 m32(float m32) {
        this.m32 = m32;
        return this;
    }

    public Matrix4 m33(float m33) {
        this.m33 = m33;
        return this;
    }

    public Matrix4 set(
        float m00, float m01, float m02, float m03,
        float m10, float m11, float m12, float m13,
        float m20, float m21, float m22, float m23,
        float m30, float m31, float m32, float m33
    ) {
        this.m00 = m00; this.m01 = m01; this.m02 = m02; this.m03 = m03;
        this.m10 = m10; this.m11 = m11; this.m12 = m12; this.m13 = m13;
        this.m20 = m20; this.m21 = m21; this.m22 = m22; this.m23 = m23;
        this.m30 = m30; this.m31 = m31; this.m32 = m32; this.m33 = m33;

        return this;
    }

    public String toString() {
        return format(
                "[%f %f %f %f | %f %f %f %f | %f %f %f %f | %f %f %f %f]",
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        );
    }

    public void store(FloatBuffer buffer) {
        buffer.put(m00);
        buffer.put(m10);
        buffer.put(m20);
        buffer.put(m30);
        buffer.put(m01);
        buffer.put(m11);
        buffer.put(m21);
        buffer.put(m31);
        buffer.put(m02);
        buffer.put(m12);
        buffer.put(m22);
        buffer.put(m32);
        buffer.put(m03);
        buffer.put(m13);
        buffer.put(m23);
        buffer.put(m33);
    }

    public Matrix4 invert() {
        float det = determinant();
        return adjoint().scale(1f/det);
    }

    private Matrix4 scale(float c) {
        return set(
                c * m00, c * m01, c * m02, c * m03,
                c * m10, c * m11, c * m12, c * m13,
                c * m20, c * m21, c * m22, c * m23,
                c * m30, c * m31, c * m32, c * m33
        );
    }

    private Matrix4 adjoint() {
        return set(
                m12*m23*m31 - m13*m22*m31 + m13*m21*m32 - m11*m23*m32 - m12*m21*m33 + m11*m22*m33,
                m03*m22*m31 - m02*m23*m31 - m03*m21*m32 + m01*m23*m32 + m02*m21*m33 - m01*m22*m33,
                m02*m13*m31 - m03*m12*m31 + m03*m11*m32 - m01*m13*m32 - m02*m11*m33 + m01*m12*m33,
                m03*m12*m21 - m02*m13*m21 - m03*m11*m22 + m01*m13*m22 + m02*m11*m23 - m01*m12*m23,

                m13*m22*m30 - m12*m23*m30 - m13*m20*m32 + m10*m23*m32 + m12*m20*m33 - m10*m22*m33,
                m02*m23*m30 - m03*m22*m30 + m03*m20*m32 - m00*m23*m32 - m02*m20*m33 + m00*m22*m33,
                m03*m12*m30 - m02*m13*m30 - m03*m10*m32 + m00*m13*m32 + m02*m10*m33 - m00*m12*m33,
                m02*m13*m20 - m03*m12*m20 + m03*m10*m22 - m00*m13*m22 - m02*m10*m23 + m00*m12*m23,

                m11*m23*m30 - m13*m21*m30 + m13*m20*m31 - m10*m23*m31 - m11*m20*m33 + m10*m21*m33,
                m03*m21*m30 - m01*m23*m30 - m03*m20*m31 + m00*m23*m31 + m01*m20*m33 - m00*m21*m33,
                m01*m13*m30 - m03*m11*m30 + m03*m10*m31 - m00*m13*m31 - m01*m10*m33 + m00*m11*m33,
                m03*m11*m20 - m01*m13*m20 - m03*m10*m21 + m00*m13*m21 + m01*m10*m23 - m00*m11*m23,

                m12*m21*m30 - m11*m22*m30 - m12*m20*m31 + m10*m22*m31 + m11*m20*m32 - m10*m21*m32,
                m01*m22*m30 - m02*m21*m30 + m02*m20*m31 - m00*m22*m31 - m01*m20*m32 + m00*m21*m32,
                m02*m11*m30 - m01*m12*m30 - m02*m10*m31 + m00*m12*m31 + m01*m10*m32 - m00*m11*m32,
                m01*m12*m20 - m02*m11*m20 + m02*m10*m21 - m00*m12*m21 - m01*m10*m22 + m00*m11*m22
        );
    }

    private float determinant() {
        return m03*m12*m21*m30 - m02*m13*m21*m30 - m03*m11*m22*m30 + m01*m13*m22*m30+
               m02*m11*m23*m30 - m01*m12*m23*m30 - m03*m12*m20*m31 + m02*m13*m20*m31+
               m03*m10*m22*m31 - m00*m13*m22*m31 - m02*m10*m23*m31 + m00*m12*m23*m31+
               m03*m11*m20*m32 - m01*m13*m20*m32 - m03*m10*m21*m32 + m00*m13*m21*m32+
               m01*m10*m23*m32 - m00*m11*m23*m32 - m02*m11*m20*m33 + m01*m12*m20*m33+
               m02*m10*m21*m33 - m00*m12*m21*m33 - m01*m10*m22*m33 + m00*m11*m22*m33;
    }

    public Matrix4 multiply(Matrix4 m) {
        return set(
                m00*m.m00 + m01*m.m10 + m02*m.m20 + m03*m.m30, m00*m.m01 + m01*m.m11 + m02*m.m21 + m03*m.m31, m00*m.m02 + m01*m.m12 + m02*m.m22 + m03*m.m32, m00*m.m03 + m01*m.m13 + m02*m.m23 + m03*m.m33,
                m10*m.m00 + m11*m.m10 + m12*m.m20 + m13*m.m30, m10*m.m01 + m11*m.m11 + m12*m.m21 + m13*m.m31, m10*m.m02 + m11*m.m12 + m12*m.m22 + m13*m.m32, m10*m.m03 + m11*m.m13 + m12*m.m23 + m13*m.m33,
                m20*m.m00 + m21*m.m10 + m22*m.m20 + m23*m.m30, m20*m.m01 + m21*m.m11 + m22*m.m21 + m23*m.m31, m20*m.m02 + m21*m.m12 + m22*m.m22 + m23*m.m32, m20*m.m03 + m21*m.m13 + m22*m.m23 + m23*m.m33,
                m30*m.m00 + m31*m.m10 + m32*m.m20 + m33*m.m30, m30*m.m01 + m31*m.m11 + m32*m.m21 + m33*m.m31, m30*m.m02 + m31*m.m12 + m32*m.m22 + m33*m.m32, m30*m.m03 + m31*m.m13 + m32*m.m23 + m33*m.m33
        );
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Matrix4)) {
            return false;
        }

        Matrix4 m = (Matrix4) o;

        return
                Math.abs(m00 - m.m00) <= 0.00001f &&
                Math.abs(m01 - m.m01) <= 0.00001f &&
                Math.abs(m02 - m.m02) <= 0.00001f &&
                Math.abs(m03 - m.m03) <= 0.00001f &&
                Math.abs(m10 - m.m10) <= 0.00001f &&
                Math.abs(m11 - m.m11) <= 0.00001f &&
                Math.abs(m12 - m.m12) <= 0.00001f &&
                Math.abs(m13 - m.m13) <= 0.00001f &&
                Math.abs(m20 - m.m20) <= 0.00001f &&
                Math.abs(m21 - m.m21) <= 0.00001f &&
                Math.abs(m22 - m.m22) <= 0.00001f &&
                Math.abs(m23 - m.m23) <= 0.00001f &&
                Math.abs(m30 - m.m30) <= 0.00001f &&
                Math.abs(m31 - m.m31) <= 0.00001f &&
                Math.abs(m32 - m.m32) <= 0.00001f &&
                Math.abs(m33 - m.m33) <= 0.00001f;
    }
}
