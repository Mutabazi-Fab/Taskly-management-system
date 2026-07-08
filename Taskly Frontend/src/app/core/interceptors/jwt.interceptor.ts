import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
    // Get the JWT token from browser's local storage
    const token = localStorage.getItem('authToken');

    // If token exists, clone the request and add the Authorization header
    if (token) {
        const authReq = req.clone({
            headers: req.headers.set('Authorization', `Bearer ${token}`)
        });
        return next(authReq);
    }

    // If no token exists, pass the original request through
    return next(req);
};
